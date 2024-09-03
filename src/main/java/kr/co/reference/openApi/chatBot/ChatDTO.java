package kr.co.reference.openApi.chatBot;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDTO {
    private String message;
    private String threadId;
    private String role;
    private String content;
    private String messageId;
    private String assistantId;
}
