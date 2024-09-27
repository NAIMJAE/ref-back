package kr.co.reference.webAnalytics.VisitorTracking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class VisitorTrackingController {

    private final VisitorTrackingService visitorTrackingService;

    @GetMapping("/visitor/count")
    public ResponseEntity<?> selectVisitorCount(){
        return visitorTrackingService.selectVisitorCount();
    }
}
