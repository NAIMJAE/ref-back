package kr.co.reference.webAnalytics.VisitorTracking;
import lombok.*;

import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CountryDTO {
    private String country;
    private int count;
    private List<RegionDTO> regions;
    private Double countryPercentage;
}
