package kr.co.reference.openApi.chatBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final ChatbotThreadRepository chatbotThreadRepository;
    private final ModelMapper modelMapper;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1";

    // Thread 생성 또는 기존 Thread 사용
    public ChatBotThreadDTO createOrGetThread(String userId) throws Exception {
        // 1. DB에서 userId로 기존 스레드 조회
        Optional<ChatBotThread> existingThread = chatbotThreadRepository.findByUid(userId);

        if (existingThread.isPresent()) {
            // 2. 기존 스레드가 있으면 해당 스레드 반환
            return modelMapper.map(existingThread.get(), ChatBotThreadDTO.class);
        }

        // 3. 기존 스레드가 없으면 새로운 Assistant 및 Thread 생성
        // String assistantId = createAssistant();
        String assistantId = "asst_1OVaAWKQbAIy8yJVinCr0RM2";
        String threadId = createNewThread(assistantId);

        // 4. 새로운 ChatBotThread 객체 생성 후 DB에 저장
        ChatBotThread newThread = ChatBotThread.builder()
                .uid(userId)
                .threadId(threadId)
                .assistantId(assistantId)
                .build();
        chatbotThreadRepository.save(newThread);

        return modelMapper.map(newThread, ChatBotThreadDTO.class);
    }

    // Assistant 생성
    public String createAssistant() throws IOException {
        log.info("Here...1");
        HttpPost httpPost = createHttpPostWithHeaders(OPENAI_API_URL + "/assistants");

        String jsonPayload = "{ \"name\": \"교포봇\", \"instructions\": \"너는 한국계 교포처럼 말하는 AI야. 대화할 때 한국어와 영어를 섞어서 사용해. 영어 감탄사나 짧은 표현을 한국어 문장 끝에 자주 덧붙이고, 가끔은 영어식 문법으로 직역된 듯한 표현을 써봐. 예를 들어, '오 마이 갓, 너 지금 like serious 하니?' 또는 '오늘 날씨 진짜 좋아, right?' 같은 식으로. 반말을 사용해서 좀 더 친구 같은 느낌을 줘.\", \"model\": \"gpt-3.5-turbo\" }";

        httpPost.setEntity(new StringEntity(jsonPayload));

        log.info("Here...2");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(result);
            // 생성된 Assistant ID 추출
            String assistantId = jsonResponse.getString("id");
            log.info("Assistant 생성됨: {}", assistantId);
            return assistantId;
        } catch (Exception e) {
            throw new IOException("Error calling OpenAI API", e);
        }
    }

    // Thread 생성
    public String createNewThread(String assistantId) throws Exception {
        log.info("Here...3 " + assistantId);
        HttpPost httpPost = createHttpPostWithHeaders(OPENAI_API_URL + "/threads");

        String jsonPayload = "{}";
        httpPost.setEntity(new StringEntity(jsonPayload));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());
            log.info("Thread 생성 응답: {}", result);

            // 응답 확인하여 요청이 성공했는지 검사
            JSONObject jsonResponse = new JSONObject(result);
            if (!jsonResponse.has("id")) {
                log.error("Thread 생성 실패: 응답에 'id'가 없습니다. 응답: {}", result);
                throw new IOException("No 'id' in response from OpenAI API");
            }

            // 요청이 성공한 경우, 응답에서 threadId를 가져옴
            String newThreadId = jsonResponse.getString("id");
            log.info("Thread 생성됨: {}", newThreadId);
            return newThreadId;
        } catch (IOException e) {
            throw new IOException("Error creating Thread", e);
        }
    }

    // Thread 실행 요청
    public String runAssistant(String threadId, String assistantId) throws Exception {
        // Run 요청을 위한 OpenAI API URL
        String runApiUrl = OPENAI_API_URL + "/threads/" + threadId + "/runs";
        log.info("Here 555 ");
        // HTTP 클라이언트 생성
        HttpPost httpPost = createHttpPostWithHeaders(runApiUrl);

        // Run 요청의 JSON 페이로드 생성
        String jsonPayload = "{ \"assistant_id\": \"" + assistantId + "\" }";
        httpPost.setEntity(new StringEntity(jsonPayload));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());
            log.info("Run 실행 응답: {}", result);

            // 응답에서 실행 요청이 성공했는지 검사
            JSONObject jsonResponse = new JSONObject(result);
            if (!jsonResponse.has("id")) {
                log.error("Run 요청 실패: 응답에 'id'가 없습니다. 응답: {}", result);
                throw new IOException("No 'id' in response from OpenAI API");
            }

            // 요청이 성공한 경우, 응답에서 Run ID를 가져옴
            String runId = jsonResponse.getString("id");
            log.info("Run 실행됨: {}", runId);
            return runId;
        } catch (IOException e) {
            throw new IOException("Error running Assistant", e);
        }
    }
    // 스레드의 모든 메시지를 조회
    public List<ChatDTO> getAllMessages(String threadId) throws Exception {
        // OpenAI API로부터 모든 메시지를 조회하는 예제
        String getMessagesUrl = OPENAI_API_URL + "/threads/" + threadId + "/messages";
        log.info("Here 777 ");
        HttpGet httpGet = new HttpGet(getMessagesUrl);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Bearer " + openaiApiKey);
        httpGet.setHeader("OpenAI-Beta", "assistants=v2");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String result = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(result);

            List<ChatDTO> messages = new ArrayList<>();

            // 응답을 파싱하여 메시지 리스트를 생성
            JSONArray messageArray = jsonResponse.getJSONArray("data");
            for (int i = 0; i < messageArray.length(); i++) {
                JSONObject messageObject = messageArray.getJSONObject(i);
                String role = messageObject.getString("role");
                JSONArray contentArray = messageObject.getJSONArray("content");
                JSONObject textObject = contentArray.getJSONObject(0).getJSONObject("text");
                String content = textObject.getString("value");
                String messageId = messageObject.getString("id");

                messages.add(ChatDTO.builder()
                        .role(role)
                        .content(content)
                        .messageId(messageId)
                        .build());
            }
            // 메시지 리스트를 역순으로 정렬
            Collections.reverse(messages);
            return messages;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
    // 메시지에 대한 답변 생성 (Chat Completion)
    public String addMessageToThread(ChatDTO chatRequest) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String addMessageUrl = OPENAI_API_URL + "/threads/" + chatRequest.getThreadId() + "/messages";
        HttpPost httpPost = createHttpPostWithHeaders(addMessageUrl);
        log.info("Here 222 ");
        // 메시지를 스레드에 추가하는 요청 본문
        String jsonPayload = "{ \"role\": \"user\", \"content\": \"" + chatRequest.getMessage() + "\" }";
        httpPost.setEntity(new StringEntity(jsonPayload, "UTF-8"));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());

            // 메시지 추가가 성공했는지 확인
            JSONObject jsonResponse = new JSONObject(result);
            if (!jsonResponse.has("id")) {
                log.error("메시지 추가 실패: 응답에 'id'가 없습니다. 응답: {}", result);
                throw new IOException("No 'id' in response from OpenAI API");
            }
            log.info("Here 333 ");
            // 추가된 메시지의 ID를 반환
            String messageId = jsonResponse.getString("id");
            log.info("메시지 추가됨: {}", messageId);
            return messageId;
        } catch (Exception e) {
            throw new IOException("Error adding message to Thread", e);
        }
    }

    // 요청생성
    private HttpPost createHttpPostWithHeaders(String url) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + openaiApiKey);
        httpPost.setHeader("OpenAI-Beta", "assistants=v2");
        return httpPost;
    }

}
