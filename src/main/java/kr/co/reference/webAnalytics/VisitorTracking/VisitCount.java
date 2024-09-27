package kr.co.reference.webAnalytics.VisitorTracking;

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
@Table(name = "vt_visit_count")
public class VisitCount {
    @Id
    private LocalDate visitDate;
    private int visitCount;
}
