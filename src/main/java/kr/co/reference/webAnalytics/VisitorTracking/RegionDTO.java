package kr.co.reference.webAnalytics.VisitorTracking;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionDTO {
    private String countryName;
    private String regionName;
    private int visitCount;
    
}
