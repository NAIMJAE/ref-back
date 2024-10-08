package kr.co.reference.openApi.chatBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatBotController {

    private final OpenAIService openAIService;

    // Assistant와 Thread를 동시에 생성
    @GetMapping("/assistant/{userId}")
    public ResponseEntity<?> createAssistantAndThread(@PathVariable String userId) {
        log.info("챗봇 생성 : " + userId);
        try {
            ChatBotThreadDTO threadDTO = openAIService.createOrGetThread(userId); // Thread 생성 또는 조회
            // Run 요청 처리
            // String runResponse = openAIService.runAssistant(threadDTO.getThreadId(), threadDTO.getAssistantId());
            return ResponseEntity.ok().body(threadDTO);
        } catch (Exception e ) {
            log.error("Error creating Assistant or Thread", e);
            return ResponseEntity.status(500).body("Error creating Assistant or Thread");
        }
    }

    // 챗봇 메시지 전송
    @PostMapping("/chatbot")
    public ResponseEntity<?> getChatResponse(@RequestBody ChatDTO chatRequest) {

        log.info("챗봇 대화 : " + chatRequest);
        try {
            // OpenAI로 메시지 추가 요청
            openAIService.addMessageToThread(chatRequest);

            // 스레드의 모든 메시지와 응답 조회
            List<ChatDTO> allMessages = openAIService.getAllMessages(chatRequest.getThreadId());

            // Run 요청으로 응답 생성
            openAIService.runAssistant(chatRequest.getThreadId(), chatRequest.getAssistantId());

            Thread.sleep(1000); // 응답생성 기다려주기

            List<ChatDTO> updateMessages = openAIService.getAllMessages(chatRequest.getThreadId());

            log.info("allMessages : " + allMessages);
            log.info("allMessages : " + allMessages.size());
            log.info("updateMessages : " + updateMessages);

            while (updateMessages == null || updateMessages.isEmpty() || allMessages.size() >= updateMessages.size()) {
                Thread.sleep(1500);
                updateMessages = openAIService.getAllMessages(chatRequest.getThreadId());
            }

            // 프론트엔드로 전체 메시지와 응답 반환
            return ResponseEntity.ok().body(updateMessages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error communicating with OpenAI API");
        }
    }

    // 메세지 전체 조회
    @GetMapping("/chatbot/{threadId}")
    public ResponseEntity<?> getChatList(@PathVariable String threadId) throws Exception {
        log.info("채팅 내역 조회 : " + threadId);
        List<ChatDTO> allMessages = openAIService.getAllMessages(threadId);

        return ResponseEntity.ok().body(allMessages);
    }
}
