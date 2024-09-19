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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataStorageService {

    private final UserRepository userRepository;
    private final UserCartRepository userCartRepository;
    private final PasswordEncoder passwordEncoder;

    // Session Login Example
    public ResponseEntity<?> dataStorageLogin(UserDTO userDTO, HttpSession session, HttpServletRequest httpRequest) {
        Cookie[] cookies = httpRequest.getCookies();
        Optional<User> optUser = userRepository.findById(userDTO.getUid());

        if (optUser.isPresent()) {
            User foundUser = optUser.get();

            if (passwordEncoder.matches(userDTO.getPassword(), foundUser.getPassword())) {
                String sessionId = session.getId();
                log.info("사용자 검증 완료 / 기존 세션 ID: " + sessionId);

                // 기존 세션 무효화 및 새 세션 추가 코드
                session.invalidate();
                session = httpRequest.getSession(true);
                log.info("세션 무효화 완료 / 새로운 세션 ID: " + session.getId());

                // 사용자 정보 Session에 추가
                session.setAttribute("userId", foundUser.getUid());
                session.setAttribute("userName", foundUser.getName());
                session.setAttribute("userRole", foundUser.getRole());
                session.setAttribute("userCart", "");

                // Header에 Cookie를 넣는 함수 호출
                HttpHeaders headers = new HttpHeaders();
                HttpHeaders addHeaders = AddCookie(foundUser, headers);

                // cookie에서 cart정보 가져오기
                Cookie cartCookie = getCookie(cookies, "REF_CART");

                if (cartCookie != null) {
                    String[] cartListArr = cartCookie.getValue().split("\\.");
                    List<UserCart> userCartList = userCartRepository.findByUid(foundUser.getUid());
                    List<String> cartProdIdList = userCartList.stream()
                            .map(UserCart::getProdId)
                            .toList();

                    // Cart에 없는 상품 DB 저장
                    for (String prodId : cartListArr) {
                        if (!cartProdIdList.contains(prodId)) {
                            UserCart newCartItem = new UserCart();
                            newCartItem.setUid(foundUser.getUid());
                            newCartItem.setProdId(prodId);
                            userCartRepository.save(newCartItem);
                        }
                    }
                }

                // userCart session 생성
                String newUserCartList = createCartSession(foundUser.getUid(), session);

                // REF_CART Cookie 초기화
                addHeaders.add(HttpHeaders.SET_COOKIE, "REF_CART=; Path=/; Max-Age=0;");
                addHeaders.add(HttpHeaders.SET_COOKIE, "REF_USER_CART=" + newUserCartList + "; Path=/;");

                // 자동 로그인 체크 (기간 7일)
                if (userDTO.isAutoLogin()) {
                    addHeaders.add(HttpHeaders.SET_COOKIE, "REF_AUTO="+ foundUser.getUid() +"; Path=/; Max-Age=604800;");
                }

                // .header() 메서드를 사용해 쿠키를 추가
                return ResponseEntity.ok().headers(addHeaders).body("SUCCESS LOGIN");
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MISS MATCH PASSWORD");
            }
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MISS MATCH USER ID");
        }
    }

    // Login Session Check
    public ResponseEntity<?> dataStorageCheck(HttpServletRequest httpRequest, HttpSession session) {
        Cookie[] cookies = httpRequest.getCookies();

        // Auto Login Check
        Cookie autoCookie = getCookie(cookies, "REF_AUTO");
        Cookie loginCookie = getCookie(cookies, "REF_LOGIN");

        // Auto Login 과 Login Cookie 가 null이 아닐 때 -> Login 처리 안됬을 때
        if (autoCookie != null && loginCookie == null) {
            String userId = autoCookie.getValue();

            Optional<User> optUser = userRepository.findById(userId);
            if (optUser.isPresent()) {
                User foundUser = optUser.get();
                session.setAttribute("userId", foundUser.getUid());
                session.setAttribute("userName", foundUser.getName());
                session.setAttribute("userRole", foundUser.getRole());

                // userCart session 생성
                String newUserCartList = createCartSession(foundUser.getUid(), session);

                HttpHeaders headers = new HttpHeaders();
                HttpHeaders addHeaders = AddCookie(foundUser, headers);
                
                // 자동 로그인 갱신
                addHeaders.add(HttpHeaders.SET_COOKIE, "REF_AUTO=" + foundUser.getUid() + "; Path=/; Max-Age=604800;");
                addHeaders.add(HttpHeaders.SET_COOKIE, "REF_USER_CART=" + newUserCartList + "; Path=/;");
                
                return ResponseEntity.ok().headers(addHeaders).body("SUCCESS LOGIN");
            }
        }

        return null;
    }

    // Management Cart
    public ResponseEntity<?> managementCart(String type, String prodId, HttpSession session) {
        String uid = (String) session.getAttribute("userId");

        if (type.equals("insert")) {
            // cart DB 조회 후 이미 추가된 상품인지 확인
            Optional<UserCart> userCartList = userCartRepository.findByUidAndProdId(uid, prodId);

            if (userCartList.isEmpty()) {
                // 없는 상품이면 DB에 추가
                UserCart newUserCart = new UserCart();
                newUserCart.setUid(uid);
                newUserCart.setProdId(prodId);

                userCartRepository.save(newUserCart);
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ALREADY PRODUCT");
            }
        } else if (type.equals("delete")) {
            // Cart DB 조회 후 추가되어 있는 상품이 맞는지 확인
            Optional<UserCart> userCartList = userCartRepository.findByUidAndProdId(uid, prodId);

            if (userCartList.isPresent()) {
                // 추가되어 있는 상품이면 DB 삭제
                userCartRepository.deleteById(userCartList.get().getCartNo());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT FOUND PRODUCT");
            }
        }

        // userCart session 생성
        String newUserCartList = createCartSession(uid, session);

        // Header에 Cookie 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "REF_USER_CART=" + newUserCartList + "; Path=/;");

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body("SUCCESS ADD PRODUCT");
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
        // 추가 쿠키(로그인 검증, 사용자 정보)를 담는 Header 생성
        headers.add(HttpHeaders.SET_COOKIE, "REF_LOGIN=true; Path=/;");
        headers.add(HttpHeaders.SET_COOKIE, "REF_INFO="+ encodedUserInfo + "; Path=/;");

        return headers;
    }

    // Create UserCart Session
    public String createCartSession(String uid, HttpSession session) {
        List<UserCart> userCartList = userCartRepository.findByUid(uid);
        List<String> cartProdIdList = userCartList.stream()
                .map(UserCart::getProdId)
                .toList();
        String newUserCartList = String.join(".", cartProdIdList);
        session.setAttribute("userCart", newUserCartList);
        return newUserCartList;
    }
}
