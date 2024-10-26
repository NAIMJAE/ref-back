package kr.co.reference.webAnalytics.VisitorTracking;
import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
public class VisitCountDTO {
    private LocalDate visitDate;
    private int visitCount;

    public VisitCountDTO(LocalDate visitDate, int visitCount) {
        this.visitDate = visitDate;
        this.visitCount = visitCount;
    }
}
