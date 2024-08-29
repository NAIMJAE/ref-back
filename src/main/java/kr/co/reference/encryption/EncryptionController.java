package kr.co.reference.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // 양방향 암호화 - 대칭키 AES 암호화
    @GetMapping("/encryption/aes")
    public ResponseEntity<?> encodeAES(@RequestParam String msg, @RequestParam int bit) {
        return encryptionService.encodeAES(msg, bit);
    }

    // 양방향 암호화 - 대칭키 AES 복호화
    @PostMapping("/encryption/aes")
    public ResponseEntity<?> decodeAES(@RequestBody Map<String, String> request) {
        return encryptionService.decodeAES(request);
    }

    // 양방향 암호화 - 비대칭키 RSA 키 생성
    @GetMapping("/encryption/rsa")
    public ResponseEntity<?> createRSAKeys() {
        return encryptionService.createRSAKeys();
    }

    // 양방향 암호화 - 비대칭키 RSA 암호화
    @PostMapping("/encryption/rsa/encode")
    public ResponseEntity<?> encodeRSA(@RequestBody Map<String, String> request) {
        String plainText = request.get("plainText");
        String keyStr = request.get("key");
        String keyType = request.get("keyType");

        if (keyType.equals("public") || keyType.equals("private")) {
            return encryptionService.encodeRSA(plainText, keyStr, keyType);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: INVALID KEY TYPE");
        }
    }

    // 양방향 암호화 - 비대칭키 RSA 복호화
    @PostMapping("/encryption/rsa/decode")
    public ResponseEntity<?> decodeRSA(@RequestBody Map<String, String> request) {
        String cipherText = request.get("cipherText");
        String keyStr = request.get("key");
        String keyType = request.get("keyType");

        if (keyType.equals("public") || keyType.equals("private")) {
            return encryptionService.decodeRSA(cipherText, keyStr, keyType);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: INVALID KEY TYPE");
        }
    }
}
