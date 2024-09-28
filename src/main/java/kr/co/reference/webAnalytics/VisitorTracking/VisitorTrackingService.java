package kr.co.reference.webAnalytics.VisitorTracking;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class VisitorTrackingService {

    private final VisitCountRepository visitCountRepository;
    private final VisitLogRepository visitLogRepository;

    // 오늘의 방문자 수, 누적 방문자 수 조회
    public ResponseEntity<?> selectVisitorCount(){
        List<VisitCount> counts = visitCountRepository.findAll();
        log.info("counts : " + counts);
        return ResponseEntity.ok().body(counts);
    }

    // 방문자 정보 저장
    public void insertVisitLog(VisitLog visitLog, String xForwarded){

        if(xForwarded != null) {
            Region location = getLocationFromIP(xForwarded);
            // Region location = getLocationFromIP("3.34.115.39");
            log.info("지역 추출 : " + location);
            visitLog.setVtCity(location.city);
            visitLog.setVtCountry(location.country);
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
