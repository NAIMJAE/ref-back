package kr.co.reference.bitCoin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
@Service
public class CoinService {

    private final BybitService bybitService;
    private final BinanceService binanceService;

    public ResponseEntity<?> getCoinList() {

        List<Coin> ByBitCoinList = bybitService.getFuturesMarket();
        List<Coin> BinanceCoinList = binanceService.getSpotMarket();

        // BinanceCoinList를 기준으로 Set 생성 (symbol 기준)
        Set<String> binanceSymbols = new HashSet<>();
        for (Coin coin : BinanceCoinList) {
            binanceSymbols.add(coin.getSymbol()); // Binance의 symbol 추가
        }

        // ByBit 목록에서 Binance의 symbol과 중복되지 않는 코인만 필터링
        List<Coin> filteredByBitCoins = new ArrayList<>();
        for (Coin coin : ByBitCoinList) {
            if (!binanceSymbols.contains(coin.getSymbol())) {
                filteredByBitCoins.add(coin); // Binance에 없는 symbol만 추가
            }
        }
        
        log.info("filteredByBitCoins : " + filteredByBitCoins);
        
        return ResponseEntity.ok().body(filteredByBitCoins);
    }


}
