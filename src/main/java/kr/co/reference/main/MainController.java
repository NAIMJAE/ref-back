package kr.co.reference.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MainController {

    private final MainService mainService;
    
    // 메인 페이지 목록 호출
    @GetMapping("/refList")
    public ResponseEntity<?> refList() {
        return mainService.refList();
    }

}
