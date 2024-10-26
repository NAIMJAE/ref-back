package kr.co.reference.webAnalytics.VisitorTracking;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;

import kr.co.reference.webAnalytics.VisitorTracking.repository.VisitCountRepository;
import kr.co.reference.webAnalytics.VisitorTracking.repository.VisitLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class VisitorTrackingService {

    private final ModelMapper modelMapper;

    private final VisitCountRepository visitCountRepository;
    private final VisitLogRepository visitLogRepository;
    private final VisitLogMapper visitLogMapper;

    // 오늘의 방문자 수, 누적 방문자 수 조회
    public ResponseEntity<?> selectVisitorCount() {

        // 일일 방문자 수 (한달)
        List<VisitCount> visitCount1Month = visitCountRepository.selectVisitCount1Month();

        // 방문자 기기 Count
        List<Map<String, Object>> deviceCount = visitLogMapper.selectDeviceCounts();

        // 방문자 방문 국가 Count
        List<CountryDTO> countries = visitLogMapper.selectCountryCounts();

        // 방문자 방문 지역 Count
        List<RegionDTO> regions = visitLogMapper.selectRegionCounts();

        // 방문자 총 합
        long totalVisitor = visitLogMapper.selectCountAll();

        List<VisitCountDTO> visitCount1MonthAll = fillMissingDates(visitCount1Month);

        // 국가별로 지역을 매칭해주는 로직
        Map<String, List<RegionDTO>> regionMap = regions.stream()
                .collect(Collectors
                        .groupingBy(region -> region.getCountryName() == null ? "unknown" : region.getCountryName()));

        if (countries.size() > 4) {

            // 상위 4개의 국가를 남기고 나머지를 '기타'로 처리
            List<CountryDTO> top4Countries = countries.subList(0, 4);
            List<CountryDTO> remainingCountries = countries.subList(4, countries.size());

            int etcCount = remainingCountries.stream().mapToInt(CountryDTO::getCount).sum();

            CountryDTO etcCountry = new CountryDTO();
            etcCountry.setCountry("etc");
            etcCountry.setCount(etcCount);

            top4Countries.add(etcCountry);

            countries = new ArrayList<>(top4Countries);
        }

        for (CountryDTO country : countries) {
            // 해당 국가의 지역 리스트를 매칭해줌
            List<RegionDTO> countryRegions = regionMap.get(
                    country.getCountry() != null ? country.getCountry() : "unknown");

            if (countryRegions != null) {
                country.setRegions(countryRegions);
            }
            // countryPercentage 계산 및 설정
            if (totalVisitor > 0) {
                double percentage = (double) country.getCount() / totalVisitor * 100;
                percentage = Math.round(percentage * 100) / 100.0;
                country.setCountryPercentage(percentage);
            } else {
                country.setCountryPercentage(0.0);
            }
        }

        VisitTrackingResponseDTO responseDTO = VisitTrackingResponseDTO.builder()
                .visitorCountForPeriod(calculateAndSetVisitorCounts(visitCount1MonthAll))
                .devicePercentage(findDevicePercentage(deviceCount))
                .countryVisitorCount(countries)
                .build();

        // 2주 (List에서 앞에 있는 14개만 가져오기)
        visitCount1MonthAll.subList(14, visitCount1MonthAll.size()).clear();
        responseDTO.setDailyVisitors(visitCount1MonthAll);

        return ResponseEntity.ok().body(responseDTO);
    }

    // 방문자 정보 저장
    public void insertVisitLog(VisitLog visitLog, String xForwarded) {

        if (xForwarded != null) {
            Region location = getLocationFromIP(xForwarded);
            // Region location = getLocationFromIP("3.34.115.39");
            log.info("지역 추출 : " + location);
            visitLog.setVtCity(location.city);
            visitLog.setVtCountry(location.country);
        } else {
            visitLog.setVtCity("Unknown");
            visitLog.setVtCountry("Unknown");
        }
        visitLogRepository.save(visitLog);
    }

    // 지역 정보를 조회하는 메소드
    public Region getLocationFromIP(String ipAddress) {
        try {
            // 리소스 폴더에 있는 GeoLite2 데이터베이스 파일 경로
            File database = new File("uploads/data/GeoLite2-City.mmdb");
            DatabaseReader dbReader = new DatabaseReader.Builder(database).build();

            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            Country country = response.getCountry();
            City city = response.getCity();

            return new Region(city.getName(), country.getName());

        } catch (Exception e) {
            log.error("GeoIP 조회 중 오류 발생: " + e.getMessage());
            return new Region("unknown", "unknown");
        }
    }

    // 비어있는 날짜를 0으로 채워주는 메서드
    public List<VisitCountDTO> fillMissingDates(List<VisitCount> visitCount1Month) {

        List<VisitCountDTO> filledVisitors = new ArrayList<>(visitCount1Month.stream()
                .map(entity -> modelMapper.map(entity, VisitCountDTO.class))
                .collect(Collectors.toList()));

        LocalDate today = LocalDate.now();
        // 이번 달의 일수를 계산
        int daysInMonth = YearMonth.from(today).lengthOfMonth();

        List<VisitCountDTO> newVisitors = new ArrayList<>(filledVisitors);
        // 한달간 반복
        for (int i = daysInMonth; i > 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            boolean equal = false;
            // dailyVisitors 리스트에 해당 날짜가 있는지 확인
            for (VisitCountDTO visitor : filledVisitors) {
                if (visitor.getVisitDate().equals(targetDate)) {
                    equal = true;
                }
            }
            if (!equal) {
                // 해당 날짜가 없다면 visit_count를 0으로 설정한 객체 추가
                newVisitors.add(new VisitCountDTO(targetDate, 0));
            }
        }
        // 날짜 최신순으로 정렬
        newVisitors.sort(Comparator.comparing(VisitCountDTO::getVisitDate).reversed());
        return newVisitors;
    }

    // 일일 방문자 수 가공
    public Map<String, Integer> calculateAndSetVisitorCounts(List<VisitCountDTO> visitCount1MonthAll) {

        Map<String, Integer> visitorCountForPeriod = new HashMap<>();

        int visitorCountSum = 0;

        for (int i = 0; i < visitCount1MonthAll.size(); i++) {
            visitorCountSum += visitCount1MonthAll.get(i).getVisitCount();

            if (i == 0) {
                visitorCountForPeriod.put("Today", visitorCountSum);
            } else if (i == 6) {
                visitorCountForPeriod.put("Week", visitorCountSum);
            }
        }

        visitorCountForPeriod.put("Month", visitorCountSum);

        return visitorCountForPeriod;
    }

    // 방문자 기기 백분율 구하는 메서드
    public Map<String, Double> findDevicePercentage(List<Map<String, Object>> deviceCount) {
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

        return devicePercentageMap;
    }
}

// 지역 정보
class Region {
    String city, country;

    Region(String city, String country) {
        this.city = city;
        this.country = country;
    }
}
