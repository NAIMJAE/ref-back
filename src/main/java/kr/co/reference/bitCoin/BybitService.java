package kr.co.reference.bitCoin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BybitService {

    @Value("${bitcoin.bybit.key}")
    private String API_KEY;
    @Value("${bitcoin.bybit.secret}")
    private String SECRET_KEY;
    private static final String BASE_URL = "https://api.bybit.com";

    public List<Coin> getFuturesMarket() {
        log.info(API_KEY);
        log.info(SECRET_KEY);
        try {
            // 1. 요청 매개변수 설정
            TreeMap<String, String> params = new TreeMap<>();
            params.put("api_key", API_KEY);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("category", "linear"); // USDT-선형 선물

            // 2. 매개변수 정렬 후 문자열로 변환
            String queryString = params.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            // 3. 서명 생성
            String signature = generateSignature(queryString, SECRET_KEY);
            params.put("sign", signature);

            // 4. 요청 URL 생성
            String fullUrl = BASE_URL + "/v5/market/instruments-info?" + buildQueryString(params);

            // 5. HTTP GET 요청 전송
            HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            // 6. 응답 처리
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 7. JSON 응답에서 필요한 정보만 추출
            List<Coin> coinList = parseAndPrintSymbols(response.toString());
            return coinList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
        // JSON 파싱 및 출력 메서드
    private static List<Coin> parseAndPrintSymbols(String jsonResponse) {
        // JSON에서 "list" 배열 추출
        String listKey = "\"list\":[";
        int listStart = jsonResponse.indexOf(listKey) + listKey.length();
        int listEnd = jsonResponse.indexOf("]", listStart);

        if (listStart == -1 || listEnd == -1) {
            System.out.println("Error: Unable to find 'list' in JSON response.");
            return null;
        }

        // "list" 배열 추출
        String listContent = jsonResponse.substring(listStart, listEnd);

        // 개별 객체 처리
        String[] items = listContent.split("\\},\\{");
        
        List<Coin> coinList = new ArrayList<>();

        for (String item : items) {
            String symbol = extractValue(item, "\"symbol\":\"");
            //String contractType = extractValue(item, "\"contractType\":\"");
            String status = extractValue(item, "\"status\":\"");

            Coin coin = new Coin(symbol, "ByBit", status);
            coinList.add(coin);
            // 기본값 처리
            //contractType = (contractType.isEmpty()) ? "N/A" : contractType;
            //System.out.printf("%s | %s | %s%n", symbol, contractType, status);
            //System.out.println(symbol);
        }
        return coinList;
    }

    // JSON에서 특정 키의 값을 추출
    private static String extractValue(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) {
            return ""; // 키가 없을 경우 빈 문자열 반환
        }
        start += key.length();
        int end = json.indexOf("\"", start);
        return (end == -1) ? "" : json.substring(start, end);
    }

    // HMAC SHA256 서명 생성 메서드
    private static String generateSignature(String queryString, String secretKey) throws Exception {
        Mac hasher = Mac.getInstance("HmacSHA256");
        hasher.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = hasher.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String hexChar = Integer.toHexString(0xff & b);
            if (hexChar.length() == 1) {
                hex.append('0');
            }
            hex.append(hexChar);
        }
        return hex.toString();
    }

    // 매개변수를 쿼리 문자열로 변환
    private static String buildQueryString(TreeMap<String, String> params) {
        return params.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }
}
