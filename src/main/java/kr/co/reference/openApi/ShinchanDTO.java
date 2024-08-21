package kr.co.reference.openApi;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShinchanDTO {
    private int charNo;
    private String charImg;
    private String charName;
    private String charGender;
    private LocalDate charBirth;
    private int charAge;
    private float charHeight;
    private float charWeight;
    private String charJob;
    private String charBloodType;
    private String charEtc;
}
