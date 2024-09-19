package kr.co.reference.main;

import jakarta.persistence.*;
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
@Table(name = "aa_reference")
public class Reference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int refNo;
    private String refTitle;
    private String refIntro;
    private String refApi;
    private String refVersion;
    @CreationTimestamp
    private LocalDate refCreate;
    @CreationTimestamp
    private LocalDate refUpdate;
    private String refThumb;
}
