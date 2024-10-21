package kr.co.reference.webAnalytics.VisitorTracking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vt_visit_count")
public class VisitCount {
    @Id
    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "visit_count")
    private int visitCount;
}
