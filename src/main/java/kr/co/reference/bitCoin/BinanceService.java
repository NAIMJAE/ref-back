package kr.co.reference.bitCoin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BinanceService {
    public List<Coin> getSpotMarket() {
        //String apiUrl = "https://fapi.binance.com/fapi/v1/exchangeInfo"; // 바이낸스 선물 API
        String apiUrl = "https://api.binance.com/api/v3/exchangeInfo"; // 바이낸스 현물 API

        try {
            // 1. URL 연결
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 연결 타임아웃
            connection.setReadTimeout(5000);    // 읽기 타임아웃

            // 2. 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                // 3. 응답 데이터 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 4. 응답 데이터 파싱
                String json = response.toString();
                List<Coin> coinList = parseSymbols(json);

                if (coinList.size() > 0) {
                    return coinList;
                }else {
                    return null;
                }

            } else {
                System.out.println("HTTP 요청 실패. 응답 코드: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Coin> parseSymbols(String json) {
        // 1. "symbols" 배열의 시작 인덱스를 찾습니다.
        int symbolsIndex = json.indexOf("\"symbols\":");
        if (symbolsIndex == -1) {
            System.out.println("JSON 데이터에서 'symbols' 배열을 찾을 수 없습니다.");
            return null;
        }

        // 2. symbols 배열의 정확한 시작과 끝을 추적합니다.
        int start = json.indexOf("[", symbolsIndex); // 배열 시작 위치
        if (start == -1) {
            System.out.println("JSON 데이터에서 배열의 시작을 찾을 수 없습니다.");
            return null;
        }

        int end = findMatchingBracket(json, start); // 배열 끝 위치
        if (end == -1) {
            System.out.println("JSON 데이터에서 배열의 끝을 찾을 수 없습니다.");
            return null;
        }

        // 3. 배열 데이터를 추출합니다.
        String symbolsArray = json.substring(start + 1, end); // 대괄호 안의 데이터

        // 4. 배열을 개별 항목으로 분리합니다.
        String[] symbolEntries = symbolsArray.split("\\},\\{");

        // 5. 모든 symbol 정보를 출력합니다.
        List<Coin> coinList = new ArrayList<>();

        for (String entry : symbolEntries) {
            entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", ""); // 앞뒤 중괄호 제거

            // "symbol" 값을 추출합니다.
            String symbol = extractValue(entry, "symbol");
            String status = extractValue(entry, "status");
            String quoteAsset = extractValue(entry, "quoteAsset");

            if (symbol != null && status != "BREAK" && quoteAsset.equals("USDT")) {
                Coin coin = new Coin(symbol, "Binance", status);
                coinList.add(coin);
            }
        }
        return coinList;
    }

    // JSON 내 대괄호의 짝을 정확히 찾는 메서드
    private static int findMatchingBracket(String json, int start) {
        int bracketCount = 0;

        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '[') {
                bracketCount++; // 열린 대괄호 증가
            } else if (json.charAt(i) == ']') {
                bracketCount--; // 닫힌 대괄호 감소
                if (bracketCount == 0) {
                    return i; // 짝이 맞는 닫힌 대괄호 위치 반환
                }
            }
        }

        return -1; // 닫힌 대괄호를 찾지 못한 경우
    }

    private static String extractValue(String entry, String key) {
        // "key":"value" 형식에서 value 추출
        int keyIndex = entry.indexOf("\"" + key + "\":");
        if (keyIndex == -1) return null;

        int start = entry.indexOf("\"", keyIndex + key.length() + 2);
        int end = entry.indexOf("\"", start + 1);

        if (start == -1 || end == -1) return null;
        return entry.substring(start + 1, end);
    }
}
