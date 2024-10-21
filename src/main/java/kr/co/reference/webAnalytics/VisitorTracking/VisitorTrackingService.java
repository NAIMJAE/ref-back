package kr.co.reference.webAnalytics.VisitorTracking;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final VisitCountRepository visitCountRepository;
    private final VisitLogRepository visitLogRepository;
    private final VisitLogMapper visitLogMapper;

    // 오늘의 방문자 수, 누적 방문자 수 조회
    public ResponseEntity<?> selectVisitorCount(){

        // 응답 객체
        VisitTrackingResponseDTO responseDTO = new VisitTrackingResponseDTO();

        // 방문자 기기 Count
        List<Map<String, Object>> deviceCount = visitLogMapper.selectDeviceCounts();

        // 방문자 방문 국가 Count
        List<CountryDTO> countries = visitLogMapper.selectCountryCounts();

        // 방문자 방문 지역 Count
        List<RegionDTO> regions = visitLogMapper.selectRegionCounts();

        log.info("deviceCount : " + deviceCount);
        log.info("country : " + countries);
        log.info("regions : " + regions);

        // 국가별로 지역을 매칭해주는 로직
        Map<String, List<RegionDTO>> regionMap = regions.stream()
        .collect(Collectors.groupingBy(region -> 
            region.getCountryName() == null ? "unknown" : region.getCountryName()));

        for (CountryDTO country : countries) {
            // 해당 국가의 지역 리스트를 매칭해줌
            List<RegionDTO> countryRegions = regionMap.get(
                country.getCountry() != null ? country.getCountry() : "unknown"
            );

            if (countryRegions != null) {
                country.setRegions(countryRegions);
            }
        }

        responseDTO.builder()
            .todayVisitorCount(100)
            .weekVisitorCount(200)
            .monthVisitorCount(300)
            .countryVisitorCount(countries)
            .build()
            .findDevicePercentage(deviceCount);
        // responseDTO.fillMissingDates();

        return ResponseEntity.ok().body("{visitDate:\"2024.10.06\",visitCount:12}");
    }

    // 방문자 정보 저장
    public void insertVisitLog(VisitLog visitLog, String xForwarded){

        if(xForwarded != null) {
            Region location = getLocationFromIP(xForwarded);
            // Region location = getLocationFromIP("3.34.115.39");
            log.info("지역 추출 : " + location);
            visitLog.setVtCity(location.city);
            visitLog.setVtCountry(location.country);
        }else{
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
}
// 지역 정보
class Region {
    String city, country;

    Region(String city, String country) {
        this.city = city;
        this.country = country;
    }
}
