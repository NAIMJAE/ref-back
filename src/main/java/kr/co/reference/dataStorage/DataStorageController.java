package kr.co.reference.dataStorage;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.reference.signUp.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;

@Slf4j
@RequiredArgsConstructor
@Controller
public class DataStorageController {

    private final DataStorageService dataStorageService;

    // Session Login Example
    @PostMapping("/datastorage/login")
    public ResponseEntity<?> dataStorageLogin(@RequestBody UserDTO userDTO, HttpSession session) {

        log.info("uid : " + userDTO.getUid());
        log.info("pw : " + userDTO.getPassword());

        return dataStorageService.dataStorageLogin(userDTO, session);
    }

    // Login Session Check
    @GetMapping("/datastorage/check")
    public ResponseEntity<?> dataStorageCheck(HttpServletRequest httpRequest, HttpSession session) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
                log.info("Cookie Name: " + cookie.getName());
                log.info("Cookie Value: " + cookie.getValue());
                log.info("Domain: " + cookie.getDomain());
                log.info("Path: " + cookie.getPath());
                log.info("Max Age: " + cookie.getMaxAge());
                log.info("Secure: " + cookie.getSecure());
                log.info("HttpOnly: " + cookie.isHttpOnly());
                log.info("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
            }
        } else {
            log.info("No Cookies Found.");
        }

        // 세션 내부 속성 조회
        log.info("세션 ID: " + session.getId());

        Enumeration<String> attributeNames = session.getAttributeNames();
        if (attributeNames.hasMoreElements()) {
            log.info("세션 내부 속성들:");
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = session.getAttribute(attributeName);
                log.info("Attribute Name: " + attributeName + ", Value: " + attributeValue);
            }
        } else {
            log.info("세션에 저장된 속성이 없습니다.");
        }

        return dataStorageService.dataStorageCheck(httpRequest, session);
    }

    // add cart
    @GetMapping("/datastorage/managementCart")
    public ResponseEntity<?> managementCart(@RequestParam String type, @RequestParam String prodId, HttpSession session) {

        log.info("type : " + type);
        log.info("type : " + prodId);

        return dataStorageService.managementCart(type, prodId, session);
    }
}
