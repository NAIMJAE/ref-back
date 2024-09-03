package kr.co.reference.main;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReferenceDTO {
    private int refNo;
    private String refTitle;
    private String refIntro;
    private String refApi;
    private String refVersion;
    private LocalDate refCreate;
    private LocalDate refUpdate;
    private String refThumb;
}
