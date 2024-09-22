package kr.co.reference.openApi;

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
@Table(name = "oa_shinchan")
public class Shinchan {
    @Id
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
