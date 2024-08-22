package kr.co.reference.openApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class OpenApiController {

    private final OpenApiService openApiService;

    // serviceKey 조회
    @GetMapping("/openApi/select/{uid}")
    public ResponseEntity<?> openApiSelectServiceKey(@PathVariable String uid) {
        log.info("uid : " + uid);

        return openApiService.openApiSelectServiceKey(uid);
    }

    // serviceKey 발급
    @GetMapping("/openApi/create/{uid}")
    public ResponseEntity<?> openApiCreateServiceKey(@PathVariable String uid) {
        log.info("uid : " + uid);

        return openApiService.openApiCreateServiceKey(uid);
    }

    // 짱구 등장인물 정보 OpenApi
    // 등장인물 데이터 조회
    @GetMapping("/shinchan/v1/getCharacterListInfo")
    public ResponseEntity<?> getCharacterListInfo(@RequestParam String serviceKey,
                                              @RequestParam String type,
                                              @RequestParam int numOfRows,
                                              @RequestParam int pageNo) {

        if (!openApiService.checkServiceKey(serviceKey)) {
            return ResponseEntity.status(HttpStatus.OK).body("SERVICE_KEY_IS_NOT_REGISTERED_ERROR");
        }else {
            return openApiService.getCharacterListInfo(type, numOfRows, pageNo);
        }
    }
    
    // 이름으로 데이터 조회
    @GetMapping("/shinchan/v1/getCharacterInfo")
    public ResponseEntity<?> getCharacterInfo(@RequestParam String serviceKey,
        @RequestParam String type,
        @RequestParam String name) {
        
        if (!openApiService.checkServiceKey(serviceKey)) {
            return ResponseEntity.status(HttpStatus.OK).body("SERVICE_KEY_IS_NOT_REGISTERED_ERROR");
        }else {
            return openApiService.getCharacterInfo(type, name);
        }
    }

}
