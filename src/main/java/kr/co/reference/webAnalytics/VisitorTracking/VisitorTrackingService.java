package kr.co.reference.webAnalytics.VisitorTracking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    public void insertVisitLog(VisitLog visitLog){
        visitLogRepository.save(visitLog);
    }
}
