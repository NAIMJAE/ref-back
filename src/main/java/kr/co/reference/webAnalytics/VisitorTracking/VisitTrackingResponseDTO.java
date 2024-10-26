package kr.co.reference.webAnalytics.VisitorTracking;

import java.util.*;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitTrackingResponseDTO {
    
    // 일일 방문자 수 (2주)
    private List<VisitCountDTO> dailyVisitors;

    // 방문자 수 요약 (오늘, 1주, 한달)
    private Map<String, Integer> visitorCountForPeriod;

    // 방문자 기기 백분률
    private Map<String, Double> devicePercentage;

    // 방문자 방문 국가, 지역 Count
    private List<CountryDTO> countryVisitorCount;

}
