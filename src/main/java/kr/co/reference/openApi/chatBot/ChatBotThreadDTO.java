package kr.co.reference.openApi.chatBot;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatBotThreadDTO {
    private String threadId;
    private String assistantId;
    private String uid;
}
