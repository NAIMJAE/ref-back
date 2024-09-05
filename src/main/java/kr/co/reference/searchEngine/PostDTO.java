package kr.co.reference.searchEngine;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO {
    private int pNo;
    private String title;
    private String contents;
    private String uid;
    private LocalDateTime rDate;

    // 추가 필드
    private String name;
    private double Related;
}
