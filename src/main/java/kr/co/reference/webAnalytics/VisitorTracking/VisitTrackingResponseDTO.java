package kr.co.reference.webAnalytics.VisitorTracking;

import java.time.LocalDate;
import java.util.*;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitTrackingResponseDTO {
    
    // 일일 방문자 수 (2주)
    private List<VisitCountDTO> dailyVisitors;

    // 방문자 수 요약 (오늘, 1주, 한달)
    private int todayVisitorCount;
    private int weekVisitorCount;
    private int monthVisitorCount;

    // 방문자 기기 백분률
    private Map<String, Double> devicePercentage;

    // 방문자 방문 국가, 지역 Count
    private List<CountryDTO> countryVisitorCount;

    // 비어있는 날짜를 0으로 채워주는 메서드
    public void fillMissingDates() {
        // 최근 14일 날짜 목록 생성
        List<VisitCountDTO> filledVisitors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 14일간 반복
        for (int i = 0; i < 14; i++) {
            LocalDate targetDate = today.minusDays(i);
            boolean found = false;
            
            // dailyVisitors 리스트에 해당 날짜가 있는지 확인
            for (VisitCountDTO visitor : dailyVisitors) {
                if (visitor.getVisitDate().equals(targetDate)) {
                    filledVisitors.add(visitor);
                    found = true;
                    break;
                }
            }
            
            // 해당 날짜가 없다면 visit_count를 0으로 설정한 객체 추가
            if (!found) {
                filledVisitors.add(new VisitCountDTO(targetDate, 0));
            }
        }

        // 날짜를 최신순으로 정렬
        Collections.reverse(filledVisitors);
        
        this.dailyVisitors = filledVisitors;
    }
    // 방문자 기기 백분율 구하는 메서드
    public void findDevicePercentage(List<Map<String, Object>> deviceCount) {
        Map<String, Double> devicePercentageMap = new HashMap<>();
        int totalVisitors = 0;

        for (Map<String, Object> device : deviceCount) {
            Long countLong = (Long) device.get("count");
            totalVisitors += countLong.intValue(); 
        }
        

        for (Map<String, Object> device : deviceCount) {
            String deviceType = (String) device.get("device");
            Long countLong = (Long) device.get("count");  
            int count = countLong.intValue();
            double percentage = (double) count / totalVisitors * 100;
            devicePercentageMap.put(deviceType, percentage);
        }

        this.devicePercentage = devicePercentageMap;
    }
}
