package kr.co.reference.webAnalytics.VisitorTracking;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.Console;
import java.time.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class VisitorTrackingInterceptor implements HandlerInterceptor {

    private final VisitorTrackingService visitorTrackingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("요청 처리 전");

        // 1) viewCount 쿠키 조회
        Cookie[] cookies = request.getCookies();
        Cookie visitRecordCookie = getCookie(cookies, "REF_VISIT_RECORD");

        // 2) 쿠키가 있을 경우 (기존 방문자)
        if (visitRecordCookie != null) {
            log.info("기존 방문자: REF_VISIT_RECORD 쿠키 존재");
            return true; // 요청 계속 진행
        }

        // 3) 쿠키가 없을 경우 (신규 방문자)
        else {
            // #1 당일 자정까지의 Max-Age 계산
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            ZonedDateTime midnight = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul")).plusDays(1);
            int maxAge = (int) (midnight.toEpochSecond() - now.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond());

            // #2 신규 Visit_Record 쿠키 생성
            ResponseCookie visitRecord = ResponseCookie.from("REF_VISIT_RECORD", now.toString() )
                    .path("/")
                    .maxAge(maxAge + 32400)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .build();

            // #3 쿠키 응답에 추가
            response.addHeader(HttpHeaders.SET_COOKIE, visitRecord.toString());

            // #4 방문자 정보 저장
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            String rawLanguage = request.getHeader("Accept-Language");
            String language = (rawLanguage != null && rawLanguage.length() >= 2) ? rawLanguage.substring(0, 5) : "unknown";
            String xForwarded = request.getHeader("X-Forwarded-For");

            log.info("userAgent : " + userAgent);
            log.info("referer : " + referer);
            log.info("language : " + language);
            log.info("xForwarded : " + xForwarded);

            VisitLog visitLog = VisitLog.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .language(language)
                    .referer(referer)
                    .vtAgent(getBrowser(userAgent))
                    .vtRegion("unknown")
                    .build();

            visitorTrackingService.insertVisitLog(visitLog, xForwarded);
        }
        return true;
    }
    // Find Cookie
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
    // 브라우저 이름 반환
    public static String getBrowser(String userAgent) {
        String browser = "";
        String userAgentLower = userAgent.toLowerCase(); // 모든 문자를 소문자로 변환

        if (userAgentLower.contains("trident") || userAgentLower.contains("msie")) {
            // IE의 버전 판별
            if (userAgentLower.contains("trident/7.0")) {
                browser = "IE 11";
            } else if (userAgentLower.contains("trident/6.0")) {
                browser = "IE 10";
            } else if (userAgentLower.contains("trident/5.0")) {
                browser = "IE 9";
            } else if (userAgentLower.contains("trident/4.0")) {
                browser = "IE 8";
            } else if (userAgentLower.contains("edge")) {
                browser = "IE Edge";
            }
        } else if (userAgentLower.contains("whale")) {
            browser = "Whale";
        } else if (userAgentLower.contains("opera") || userAgentLower.contains("opr")) {
            browser = "Opera";
        } else if (userAgentLower.contains("firefox")) {
            browser = "Firefox";
        } else if (userAgentLower.contains("chrome")) {
            if (userAgentLower.contains("edg")) {
                browser = "Edge";
            } else {
                browser = "Chrome";
            }
        } else if (userAgentLower.contains("safari") && !userAgentLower.contains("chrome")) {
            browser = "Safari";
        } else {
            browser = "Other";
        }
        log.info("browser : " + browser);
        return browser;
    }
}
