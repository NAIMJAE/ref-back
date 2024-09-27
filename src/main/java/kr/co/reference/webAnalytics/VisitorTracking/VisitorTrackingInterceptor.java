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
        Cookie visitRecordCookie = getCookie(cookies, "REF_Visit_Record");

        // 2) 쿠키가 있을 경우 (기존 방문자)
        if (visitRecordCookie != null) {
            log.info("기존 방문자: REF_Visit_Record 쿠키 존재");
            return true; // 요청 계속 진행
        }

        // 3) 쿠키가 없을 경우 (신규 방문자)
        else {
            // #1 당일 자정까지의 Max-Age 계산
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            ZonedDateTime midnight = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul")).plusDays(1);
            int maxAge = (int) (midnight.toEpochSecond() - now.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond());

            // #2 신규 Visit_Record 쿠키 생성
            ResponseCookie visitRecord = ResponseCookie.from("REF_Visit_Record", now.toString() )
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
            String language = request.getHeader("Accept-Language");
            String xForwarded = request.getHeader("X-Forwarded-For");
            String region = request.getRemoteAddr();

            log.info("userAgent : " + userAgent);
            log.info("referer : " + referer);
            log.info("language : " + language);
            log.info("xForwarded : " + xForwarded);
            log.info("region : " + region);

            VisitLog visitLog = VisitLog.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .language(language)
                    .referer(referer)
                    .vtAgent(userAgent)
                    .vtRegion(region)
                    .build();

            visitorTrackingService.insertVisitLog(visitLog);
        }
        return true;
    }
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
}
