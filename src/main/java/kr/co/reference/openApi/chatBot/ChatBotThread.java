package kr.co.reference.openApi.chatBot;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cb_thread")
public class ChatBotThread {

    @Id
    private String threadId;

    private String assistantId;

    private String uid;
}
