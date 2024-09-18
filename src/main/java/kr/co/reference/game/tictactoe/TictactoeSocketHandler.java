package kr.co.reference.game.tictactoe;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class TictactoeSocketHandler extends TextWebSocketHandler {
    // 방 별로 세션을 관리하기 위한 ConcurrentMap<방번호, Map<Socket세션ID, WebSocketSession>>
    // 0 - 대기실 / 1, 2, 3 - 게임방
    private static final ConcurrentMap<Integer, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // JSON 변환을 위한 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 웹 소켓 클라이언트와 연결
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // 1) 접속한 사용자의 소켓세션을 저장
        Map<String, WebSocketSession> lobby = roomSessions.computeIfAbsent(0, k -> new ConcurrentHashMap<>());
        lobby.put(session.getId(), session);

        // 2) 모든 사용자에게 현재 게임방 인원수 체크해서 전달
        broadcastRoomStatus();
    }

    // 웹 소켓이 클라이언트로부터 메세지를 전송받았을 때 실행되는 메서드
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 받은 메시지를 JSON으로 파싱
        Map<String, Object> messageMap = objectMapper.readValue(message.getPayload(), HashMap.class);

        // 메시지에 이미 type 필드가 포함되어 있는 경우 처리
        String type = (String) messageMap.get("type");
        int newRoomId = (Integer) messageMap.get("roomId");
        log.info("newRoomId : " + newRoomId);

        // 1) 게임방에 접속했을때 (+ 방을 이동했을때)
        if("moveRoom".equals(type)) {
            // #1 roomSessions 방번호를 매칭
            int oldRoomId = -1;
            for (Map.Entry<Integer, Map<String, WebSocketSession>> entry : roomSessions.entrySet()) {
                if (entry.getValue().remove(session.getId()) != null) {
                    oldRoomId = entry.getKey();
                    break;
                }
            }

            Map<String, WebSocketSession> newRoom = roomSessions.computeIfAbsent(newRoomId, k -> new ConcurrentHashMap<>());
            newRoom.put(session.getId(), session);

            // 현재 방에 있는 사용자의 수를 체크
            int playerCount = roomSessions.get(newRoomId).size();
            if (playerCount > 2) {
                newRoom.remove(session.getId());
                // 정원 초과 메시지를 전송
                Map<String, Object> response = new HashMap<>();
                response.put("type", "full");
                response.put("message", "Room is full");
                String jsonResponse = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(jsonResponse));

                // 로그 출력 후 연결 종료
                log.info("방이 가득 찼습니다. : " + session.getId());
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }else{
                // #1 - 1 게임방에 들어가 있는 상태였다면 기존 방에 들어가있는 상대 플레이어 심볼 업데이트
                if (oldRoomId != 0 && roomSessions.containsKey(oldRoomId)) {
                    Map<String, Object> updateSymbolMessage = new HashMap<>();
                    updateSymbolMessage.put("type", "opponentLeft");
                    updateSymbolMessage.put("player", 0);

                    // 같은 방의 유저에게 메시지 전송
                    multicast(oldRoomId, updateSymbolMessage);
                }

                // #2 플레이어 심볼을 계산해서 전달 (0: 먼저 움직임, 1: 나중에 움직임)
                Map<String, Object> response = new HashMap<>();
                response.put("type", "moveRoom");
                response.put("roomId", newRoomId);
                response.put("player", playerCount);


                String jsonResponse = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(jsonResponse));

                // #3 모든 사용자에게 현재 게임방 인원수 체크해서 전달
                broadcastRoomStatus();
            }

        }

        // 2) 게임중일 때 (그대로 전달)
        else if ("makeMove".equals(type)) {
            log.info("게임 진행 : "+ messageMap);
            multicast(newRoomId, messageMap);
        }
        // 3) 게임이 끝났을 때
        else if ("gameOver".equals(type)) {
            // #1 게임 결과 전달
            multicast(newRoomId, messageMap);

            // #2 해당 방의 모든 플레이어를 방에서 제거하고 대기실로 이동
            Map<String, WebSocketSession> room = roomSessions.get(newRoomId);
            if (room != null) {
                for (WebSocketSession playerSession : room.values()) {
                    Map<String, WebSocketSession> lobby = roomSessions.computeIfAbsent(0, k -> new ConcurrentHashMap<>());
                    lobby.put(playerSession.getId(), playerSession);
                }
                room.clear();
            }
            multicast(newRoomId, messageMap);
        }
        // 4) 방을 나갈 때
        else if ("leaveRoom".equals(type)) {

            // #1 상대방에게 플레이어가 떠났음을 알림
            if (newRoomId != 0) {
                Map<String, Object> opponentLeftMessage = new HashMap<>();
                opponentLeftMessage.put("type", "opponentLeft");

                multicast(newRoomId, opponentLeftMessage);
            }

            // #2 세션을 대기실로 이동
            Map<String, WebSocketSession> lobby = roomSessions.computeIfAbsent(0, k -> new ConcurrentHashMap<>());
            lobby.put(session.getId(), session);

            // #4 방 상태 업데이트
            broadcastRoomStatus();
        }
    }

    // 웹 소켓이 클라이언트와 연결이 끊겼을 때 실행되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        int roomId = -1; // 플레이어가 속한 방 번호를 저장하기 위한 변수

        // 1) 게임방에 저장되어 있는 해당 플레이어 세션 만료시키기
        for (Map.Entry<Integer, Map<String, WebSocketSession>> entry : roomSessions.entrySet()) {
            if (entry.getValue().remove(session.getId()) != null) {
                roomId = entry.getKey(); // 세션이 속한 방 번호를 저장합니다.
                log.info("나갈 RoomId : " + roomId);
                break;
            }
        }

        // 2) 모든 사용자에게 현재 게임방 인원수 체크해서 전달
        broadcastRoomStatus();

        // 3) 같은 룸에 속한 사람의 플레이어 심볼 업데이트
        if (roomId != -1 && roomId != 0) {
            Map<String, WebSocketSession> room = roomSessions.get(roomId);
            if (room != null && room.size() > 0) {
                Map<String, Object> updateSymbolMessage = new HashMap<>();
                updateSymbolMessage.put("type", "opponentLeft");
                updateSymbolMessage.put("player", 0);

                // 같은 방에 남아 있는 사용자에게 메시지 전송
                multicast(roomId, updateSymbolMessage);
            }
        }
    }

    // 모든 클라이언트에게 현재 게임방 인원수 체크해서 전송하는 메서드
    private void broadcastRoomStatus() {
        try {
            Map<Integer, Object> roomData = new HashMap<>();
            for(int roomId : roomSessions.keySet()){
                roomData.put(roomId, roomSessions.get(roomId).size());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("type", "roomlist");
            response.put("rooms", roomData);

            String jsonResponse = objectMapper.writeValueAsString(response);

            for (Map<String, WebSocketSession> sessions : roomSessions.values()) {
                for (WebSocketSession webSocketSession : sessions.values()) {
                    if (webSocketSession.isOpen()) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(jsonResponse));
                            log.info("메시지 전송 성공: " + webSocketSession.getId());
                        } catch (Exception e) {
                            log.error("메시지 전송 실패: " + webSocketSession.getId(), e);
                        }
                    } else {
                        log.warn("세션이 닫혀 있음: " + webSocketSession.getId());
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    // 같은 방 유저에게 전송하는 메서드
    private void multicast(int roomId, Map<String, Object> messageMap) {
        try {
            for (WebSocketSession webSocketSession : roomSessions.get(roomId).values()) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageMap)));
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }
}
