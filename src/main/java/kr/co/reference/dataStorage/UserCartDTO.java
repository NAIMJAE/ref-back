package kr.co.reference.dataStorage;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCartDTO {
    private int cartNo;
    private String uid;
    private String prodId;
}
