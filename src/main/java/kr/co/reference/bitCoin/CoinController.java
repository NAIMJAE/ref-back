package kr.co.reference.bitCoin;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@RequiredArgsConstructor
@Controller
public class CoinController {

    private final CoinService coinService;
    
    @GetMapping("/getCoinList")
    public ResponseEntity<?> getCoinList() {

        return coinService.getCoinList();
    }
    
}
