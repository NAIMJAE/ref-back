package kr.co.reference.signUp;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private String uid;
    private String password;
    private String name;
    private String role;
    private String serviceKey;
    private LocalDate rdate;

    // 추가 필드
    private boolean autoLogin;
}
