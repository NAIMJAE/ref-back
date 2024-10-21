package kr.co.reference.webAnalytics.VisitorTracking;
import lombok.*;

import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
public class CountryDTO {
    private String country;
    private int count;
    private List<RegionDTO> regions;

    public CountryDTO(String country, int count, List<RegionDTO> regions) {
        this.country = country;
        this.count = count;
        this.regions = regions;
    }

}
