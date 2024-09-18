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
public class TictactoeSocketHandler22 extends TextWebSocketHandler {
    // 방 별로 세션을 관리하기 위한 ConcurrentMap
    // 게임 플레이할 때 사용
    private static final ConcurrentMap<String, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 현재 같은 소켓에 연결된 사용자 목록을 관리하기 위한 ConcurrentMap
    // 방 인원 관리할 때 사용
    private final Map<String, List<String>> memberSessions = new ConcurrentHashMap<>();

    // JSON 변환을 위한 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 웹 소켓 클라이언트와 연결
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        log.info("Here ... 1");
        // 방별 현재 인원수 보내주기
        Map<String, Object> response = new HashMap<>();
        response.put("type", "roomlist");

        // 초기 연결 시 roomSessions에 세션 추가
        roomSessions.computeIfAbsent("", k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        session.getAttributes().put("roomId", "");

        Map<String, Object> roomData = new HashMap<>();
        for (String roomId : memberSessions.keySet()) {
            roomData.put(roomId, memberSessions.get(roomId).size());
        }
        response.put("rooms", roomData);
        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
        // 방에 새로운 플레이어 추가
        log.info("연결된 소켓 세션 아이디 : " + session.getId());

    }

    // 웹 소켓이 클라이언트로부터 메세지를 전송받았을 때 실행되는 메서드
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 받은 메시지를 JSON으로 파싱
        Map<String, Object> messageMap = objectMapper.readValue(message.getPayload(), HashMap.class);

        // 메시지에 이미 type 필드가 포함되어 있는 경우 처리
        String type = (String) messageMap.get("type");

        log.info("소켓 메세지 타입 : "+ type);

        if ("leaveRoom".equals(type)) {
            // 클라이언트의 게임 종료
            String userId = (String) messageMap.get("userId");
            String roomId = String.valueOf(messageMap.get("roomId"));

            log.info("leaveRoom userId : " + userId + " | roomId : " + roomId);

            // 기존 방에서 사용자 제거
            if (roomId != null && !roomId.isEmpty()) {
                List<String> roomMembers = memberSessions.get(roomId);
                if (roomMembers != null) {
                    roomMembers.remove(userId);
                }
            }

            // 같은 방의 플레이어의 심볼 변경 전송
            // 방에 남은 사용자가 있으면, 해당 사용자에게 새로운 역할(0)을 할당
            if (!memberSessions.get(roomId).isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "playerLeft");
                response.put("player", 0);
                String jsonResponse = objectMapper.writeValueAsString(response);

                try {
                   log.info("심볼 변경 전송");
                   log.info("roomId : " + roomId);
                   log.info("roomSessions : " + roomSessions);
                   log.info("roomSessions.get(roomId) : " + roomSessions.get(roomId));
                   log.info("roomSessions.values() : " + roomSessions.values());



                    for (WebSocketSession webSocketSession : roomSessions.get(roomId).values()) {
                        log.info("심볼 변경 전송 22 ");
                        if (webSocketSession.isOpen()) {
                            webSocketSession.sendMessage(new TextMessage(jsonResponse));
                        }
                    }
                }catch (Exception e) {
                    log.error(e.getMessage());
                }
            }

            // 모든 클라이언트에게 현재 방들의 상태를 업데이트하는 메시지 전송
            broadcastRoomStatus();

        }else if("moveRoom".equals(type)) {
            // 클라이언트의 방 이동 요청
            String userId = (String) messageMap.get("userId");
            String newRoomId = String.valueOf(messageMap.get("roomId"));

            log.info("userId : " + userId);
            log.info("roomId : " + newRoomId);

            // 사용자의 기존 방 ID를 가져옴
            String oldRoomId = (String) session.getAttributes().get("roomId");

            // 기존 방에서 사용자 제거
            if (oldRoomId != null && !oldRoomId.isEmpty()) {
                List<String> oldRoomMembers = memberSessions.get(oldRoomId);
                if (oldRoomMembers != null) {
                    oldRoomMembers.remove(userId);
                    log.info("사용자 제거됨: " + userId + " from room: " + oldRoomId);
                }
                Map<String, WebSocketSession> oldRoomSessions = roomSessions.get(oldRoomId);
                if (oldRoomSessions != null) {
                    oldRoomSessions.remove(session.getId());
                    log.info("세션 제거됨: " + session.getId() + " from room: " + oldRoomId);
                }
            }

            session.getAttributes().put("roomId", newRoomId);

            // 현재 방에 있는 사용자의 수를 체크
            int playerCount = memberSessions.computeIfAbsent(newRoomId, k -> new ArrayList<>()).size();
            log.info("playerCount : " + playerCount);
            if (playerCount >= 2) {
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
            }

            // memberSessions에 사용자를 추가
            /*
            * - computeIfAbsent :
            *   roomId가 memberSessions 맵에 이미 존재하는지 확인
            *   만약 존재하지 않으면, 람다식 k -> new ArrayList<>()가 실행되어 new ArrayList<>()를 생성하고, 이를 roomId에 대한 값으로 맵에 추가
            */
            memberSessions.computeIfAbsent(newRoomId, k -> new ArrayList<>()).add(userId);

            // roomSessions에 WebSocketSession 추가
            log.info("newRoomId : " + newRoomId);
            log.info("session.getId() : " + session.getId());

            roomSessions.computeIfAbsent(newRoomId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);

            log.info("roomSessions.get(1) : " + roomSessions.get("1"));
            log.info("roomSessions.get(2) : " + roomSessions.get("2"));
            log.info("roomSessions.get(3) : " + roomSessions.get("3"));

            // 클라이언트에게 역할을 전달 (0: 먼저 움직임, 1: 나중에 움직임)
            Map<String, Object> response = new HashMap<>();
            response.put("type", "moveRoom");
            response.put("roomId", newRoomId);
            response.put("player", playerCount);

            String jsonResponse = objectMapper.writeValueAsString(response);

            log.info("역할 전달 : " + jsonResponse);
            session.sendMessage(new TextMessage(jsonResponse));
            // 모든 클라이언트에게 현재 방들의 상태를 업데이트하는 메시지 전송
            broadcastRoomStatus();
        }else if ("makeMove".equals(type) || "gameOver".equals(type)) {
            // 메시지의 타입에 따라 처리
            String roomId = (String) messageMap.get("roomId");

            log.info("makeMove roomId : " + roomId);
            log.info("makeMove roomSessions : " + roomSessions);
            log.info("makeMove roomSessions.get(roomId) : " + roomSessions.get(roomId));

            // 같은 방에 있는 모든 클라이언트에게 메시지 방송
            for (WebSocketSession webSocketSession : roomSessions.get(roomId).values()) {

                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageMap)));
                }
            }
        }
    }

    // 웹 소켓이 클라이언트와 연결이 끊겼을 때 실행되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);  // 사용자의 현재 방 ID를 가져옵니다.

        if (roomId != null) {
            Map<String, WebSocketSession> sessions = roomSessions.get(roomId);
            // 세션 제거
            if (sessions != null) {
                sessions.remove(session.getId());
            }
        }
    }

    // roomId를 session에서 가져오는 메서드
    private String getRoomId(WebSocketSession session) {
        return (String) session.getAttributes().get("roomId");
    }

    // 모든 클라이언트에게 방 상태를 전송하는 메서드
    private void broadcastRoomStatus() {
        try {
            log.info("브로드캐스트");
            Map<String, Object> response = new HashMap<>();
            response.put("type", "roomlist");

            Map<String, Object> roomData = new HashMap<>();
            for (String roomId : memberSessions.keySet()) { // memberSessions 사용
                log.info("roomId : " + roomId);
                log.info("roomId size: " + memberSessions.get(roomId).size());
                roomData.put(roomId, memberSessions.get(roomId).size());
            }
            response.put("rooms", roomData);

            String jsonResponse = objectMapper.writeValueAsString(response);

            // 모든 클라이언트에게 방 상태 전송
            log.info("roomSessions.values() : " + roomSessions.values());
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
        } catch (Exception e) {
            log.error("브로드캐스트 중 예외 발생: ", e);
        }
    }

}
