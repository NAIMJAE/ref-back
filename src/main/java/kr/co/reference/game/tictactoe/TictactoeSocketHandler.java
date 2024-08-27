package kr.co.reference.game.tictactoe;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class TictactoeSocketHandler extends TextWebSocketHandler {
    // 방 별로 세션을 관리하기 위한 ConcurrentMap
    private static final ConcurrentMap<String, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 현재 같은 소켓에 연결된 사용자 목록을 관리하기 위한 ConcurrentMap
    private final Map<String, List<String>> memberSessions = new ConcurrentHashMap<>();

    // JSON 변환을 위한 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 웹 소켓이 클라이언트와 연결된 후 실행되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 소켓 연결 URI에서 방 번호 추출
        String roomId = getRoomId(session);
        Map<String, WebSocketSession> sessions = roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        log.info("roomId : " + roomId);
        // 현재 방에 있는 사용자의 수를 체크
        int playerCount = sessions.size();

        // 방이 가득 찼는지 확인
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

        // roomSessions에 방 정보 세션 저장
        roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);

        // 방에 새로운 플레이어 추가
        sessions.put(session.getId(), session);

        // 클라이언트에게 역할을 전달 (0: 먼저 움직임, 1: 나중에 움직임)
        Map<String, Object> response = new HashMap<>();
        response.put("type", "assignRole");
        response.put("player", playerCount);
        String jsonResponse = objectMapper.writeValueAsString(response);

        log.info("역할 전달 : " + jsonResponse);
        session.sendMessage(new TextMessage(jsonResponse));

        log.info("연결된 소켓 세션 아이디 : " + session.getId() + " 방 번호 : " + roomId + " 현재 인원: " + sessions.size());
    }

    // 웹 소켓이 클라이언트로부터 메세지를 전송받았을 때 실행되는 메서드
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomId(session);
        System.out.println("받은 메세지 : " + message.getPayload() + " 방 번호 : " + roomId);

        // 받은 메시지를 JSON으로 파싱
        Map<String, Object> messageMap = objectMapper.readValue(message.getPayload(), HashMap.class);

        // 메시지에 이미 type 필드가 포함되어 있는 경우 처리
        String type = (String) messageMap.get("type");

        // 메시지의 타입에 따라 처리
        if ("makeMove".equals(type) || "gameOver".equals(type)) {
            // 같은 방에 있는 모든 클라이언트에게 메시지 방송
            for (WebSocketSession webSocketSession : roomSessions.get(roomId).values()) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageMap)));
                }
            }
        } else {
            // 다른 타입의 메시지 처리
            System.out.println("알 수 없는 메시지 타입: " + type);
        }
    }


    // 웹 소켓이 클라이언트와 연결이 끊겼을 때 실행되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);

        Map<String, WebSocketSession> sessions = roomSessions.get(roomId);

        // 세션 제거
        if (sessions != null) {
            sessions.remove(session.getId());
            log.info("연결이 종료된 소켓 세션 아이디 : " + session.getId() + " 방 번호 : " + roomId + " 남은 인원: " + sessions.size());

            // 방에 남은 사용자가 있으면, 해당 사용자에게 새로운 역할(0)을 할당
            if (!sessions.isEmpty()) {
                WebSocketSession remainingSession = sessions.values().iterator().next();
                Map<String, Integer> response = new HashMap<>();
                response.put("player", 0);
                String jsonResponse = objectMapper.writeValueAsString(response);
                remainingSession.sendMessage(new TextMessage(jsonResponse));
            }
        }
        log.info("연결을 종료할 소켓 세션 아이디 : " + session.getId() + " 방 번호 : " + roomId);
    }

    // URI에서 roomId를 추출
    private String getRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.split("/")[3];
    }
}
