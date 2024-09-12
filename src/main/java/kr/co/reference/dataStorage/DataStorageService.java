package kr.co.reference.dataStorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.reference.signUp.User;
import kr.co.reference.signUp.UserDTO;
import kr.co.reference.signUp.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataStorageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Session Login Example
    public ResponseEntity<?> dataStorageLogin(@RequestBody UserDTO userDTO, HttpSession session) {

        Optional<User> optUser = userRepository.findById(userDTO.getUid());
        if (optUser.isPresent()) {
            User foundUser = optUser.get();

            if (passwordEncoder.matches(userDTO.getPassword(), foundUser.getPassword())) {

                String sessionId = session.getId();
                log.info("사용자 검증 완료 / 기존 세션 ID: " + sessionId);

                // 기존 세션 무효화 및 새 세션 추가 코드 필요
                // session.invalidate();  

                // 사용자 정보 Session에 추가
                session.setAttribute("userId", foundUser.getUid());
                session.setAttribute("userName", foundUser.getName());
                session.setAttribute("userRole", foundUser.getRole());

                // Header에 Cookie를 넣는 함수 호출
                HttpHeaders headers = new HttpHeaders();
                HttpHeaders addHeaders = AddCookie(foundUser, headers);

                // 자동 로그인 체크 (기간 7일)
                if (userDTO.isAutoLogin()) {
                    log.info("ㅎ2");
                    addHeaders.add(HttpHeaders.SET_COOKIE, "REF_AUTO="+ foundUser.getUid() +"; Path=/; Max-Age=604800;");
                }

                // .header() 메서드를 사용해 쿠키를 추가
                return ResponseEntity.ok().headers(addHeaders).body("로그인 성공");

            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MISS MATCH PASSWORD");
            }
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NOT FOUND USER");
        }
    }

    // Login Session Check
    public ResponseEntity<?> dataStorageCheck(HttpServletRequest httpRequest, HttpSession session) {
        Cookie[] cookies = httpRequest.getCookies();

        // Auto Login Check
        Cookie autoCookie = getCookie(cookies, "REF_AUTO");

        if (autoCookie != null) {
            String userId = autoCookie.getValue();

            Optional<User> optUser = userRepository.findById(userId);
            if (optUser.isPresent()) {
                User foundUser = optUser.get();
                session.setAttribute("userId", foundUser.getUid());
                session.setAttribute("userName", foundUser.getName());
                session.setAttribute("userRole", foundUser.getRole());

                HttpHeaders headers = new HttpHeaders();
                HttpHeaders addHeaders = AddCookie(foundUser, headers);
                
                // 자동 로그인 갱신
                addHeaders.add(HttpHeaders.SET_COOKIE, "REF_AUTO="+ foundUser.getUid() +"; Path=/; Max-Age=604800;");
                return ResponseEntity.ok().headers(addHeaders).body("로그인 성공");
            }
        }

        //

        return null;
    }


    // Additional Method
    // Find cookie
    public Cookie getCookie(Cookie[] cookies, String cookieName) {
        if (cookies != null) {
            // 쿠키 배열을 순회하며 특정 이름의 쿠키를 찾습니다.
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    // Add Cookie Data
    public HttpHeaders AddCookie(User foundUser, HttpHeaders headers) {
        // 사용자 정보를 JSON으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", foundUser.getUid());
        userInfo.put("userName", foundUser.getName());
        userInfo.put("userRole", foundUser.getRole());

        // 사용자 정보를 담은 쿠키를 만들기 위해 사용자 정보 JSON 변환 & 인코딩
        String encodedUserInfo;
        try {
            String userInfoJson = objectMapper.writeValueAsString(userInfo);
            encodedUserInfo = URLEncoder.encode(userInfoJson, StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            return null;
        }
        log.info("ㅎ1");
        // 추가 쿠키(로그인 검증, 사용자 정보)를 담는 Header 생성
        headers.add(HttpHeaders.SET_COOKIE, "REF_LOGIN=true; Path=/;");
        headers.add(HttpHeaders.SET_COOKIE, "REF_INFO="+ encodedUserInfo + "; Path=/;");

        return headers;
    }
}
