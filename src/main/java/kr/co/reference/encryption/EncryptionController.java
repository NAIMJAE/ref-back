package kr.co.reference.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class EncryptionController {

    private final EncryptionService encryptionService;

    // 단방향 암호화 SHA-256
    @GetMapping("/encryption/sha256/{msg}")
    public ResponseEntity<?> encodeSHA256(@PathVariable String msg) {
        log.info("msg : " + msg);
        return encryptionService.encodeSHA256(msg);
    }

    // 단방향 암호화 BCrypt
    @GetMapping("/encryption/bcrypt")
    public ResponseEntity<?> encodeBCrypt(@RequestParam String msg, @RequestParam int work) {

        return encryptionService.encodeBCrypt(msg, work);
    }

}
