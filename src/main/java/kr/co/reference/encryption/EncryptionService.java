package kr.co.reference.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class EncryptionService {

    // 단방향 암호화 SHA-256
    public ResponseEntity<?> encodeSHA256(String msg) {
        StringBuilder hexString = new StringBuilder();

        try {
            MessageDigest MD = MessageDigest.getInstance("SHA-256");
            byte[] hash = MD.digest(msg.getBytes());

            log.info("hash : " + Arrays.toString(hash));

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            log.info("hexString : " + hexString);

        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(hexString);
    }

    // 단방향 암호화 BCrypt
    public ResponseEntity<?> encodeBCrypt(String msg, int work) {

        // 비밀번호 해싱
        String hashedPassword = BCrypt.hashpw(msg, BCrypt.gensalt(work));
        log.info("Hashed Password: " + hashedPassword);

        // 비밀번호 검증
        boolean isPasswordMatch = BCrypt.checkpw(msg, hashedPassword);
        log.info("Password matches: " + isPasswordMatch);

        return ResponseEntity.status(HttpStatus.OK).body(hashedPassword);
    }

}
