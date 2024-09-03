package kr.co.reference.openApi.chatBot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotThreadRepository extends JpaRepository<ChatBotThread, String> {

    // 유저의 스레드 조회
    public Optional<ChatBotThread> findByUid(String uid);
}
