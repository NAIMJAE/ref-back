package kr.co.reference.openApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.reference.signUp.User;
import kr.co.reference.signUp.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenApiService {

    private final UserRepository userRepository;
    private final ShinchanRepository shinchanRepository;
    private final ObjectMapper objectMapper;

    // serviceKey 조회
    public ResponseEntity<?> openApiSelectServiceKey(String uid) {

        Optional<User> optUser = userRepository.findById(uid);

        if (optUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(optUser.get().getServiceKey());
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    // serviceKey 발급
    public ResponseEntity<?> openApiCreateServiceKey(String uid) {

        Optional<User> optUser = userRepository.findById(uid);

        if (optUser.isPresent()) {
            String key = UUID.randomUUID().toString();
            optUser.get().setServiceKey(key);
            userRepository.save(optUser.get());
            return ResponseEntity.status(HttpStatus.OK).body(key);
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    // 인증키 검사
    public boolean checkServiceKey(String serviceKey) {
        Optional<User> optUser = userRepository.findByServiceKey(serviceKey);
        if (optUser.isPresent()) {
            return true;
        }else {
            return false;
        }
    }

    // 등장인물 데이터 조회
    public ResponseEntity<?> getCharacterListInfo(String type, int numOfRows, int pageNo) {

        List<Shinchan> characterList = shinchanRepository.selectShinchanList(numOfRows, pageNo);

        if(characterList.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body("PAGE_NUMBER_EXCEEDS_TOTAL_PAGES");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", characterList);
        data.put("numOfRows", numOfRows);
        data.put("pageNo", pageNo);

        if (type.equals("json")) {
            try {
                String result = objectMapper.writeValueAsString(data);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }catch (JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.OK).body("UNKNOWN_ERROR");
            }
        }else {
            return ResponseEntity.status(HttpStatus.OK).body("INVALID_REQUEST_PARAMETER_ERROR_TYPE");
        }
    }

    // 이름으로 데이터 조회
    public ResponseEntity<?> getCharacterInfo(String type, String name) {
        Optional<Shinchan> optShinchan = shinchanRepository.findByCharName(name);

        if (optShinchan.isPresent()) {
            if (type.equals("json")) {
                try {
                    String result = objectMapper.writeValueAsString(optShinchan.get());
                    return ResponseEntity.status(HttpStatus.OK).body(result);
                }catch (JsonProcessingException e) {
                    return ResponseEntity.status(HttpStatus.OK).body("UNKNOWN_ERROR");
                }
            }else {
                return ResponseEntity.status(HttpStatus.OK).body("INVALID_REQUEST_PARAMETER_ERROR_TYPE");
            }
        }else {
            return ResponseEntity.status(HttpStatus.OK).body("INVALID_REQUEST_PARAMETER_ERROR_NAME");
        }
    }



}
