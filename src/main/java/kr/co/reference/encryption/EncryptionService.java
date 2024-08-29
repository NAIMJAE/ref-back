package kr.co.reference.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class EncryptionService {

    // 단방향 암호화 SHA-256
    public ResponseEntity<?> encodeSHA256(String plainText) {
        StringBuilder hexString = new StringBuilder();

        try {
            MessageDigest MD = MessageDigest.getInstance("SHA-256");
            byte[] hash = MD.digest(plainText.getBytes());

            log.info("hash : " + Arrays.toString(hash));

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            log.info("hexString : " + hexString);

            return ResponseEntity.status(HttpStatus.OK).body(hexString);

        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: ENCRYPTION FAIL");
        }
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

    // 양방향 암호화 - 대칭키 AES 암호화
    public ResponseEntity<?> encodeAES(String msg, int bit) {

        // AES 키 생성
        SecretKey secretKey = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(bit); // 128, 192, 256 비트 중 하나를 선택 가능
            secretKey = keyGenerator.generateKey();
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: KEY GENERATION FAILED");
        }

        // IV(Initial Vector) 생성 (AES CBC 모드에서 필요)
        byte[] iv = new byte[16];
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // AES 암호화
        String cipherText = encrypt(msg, secretKey, ivParameterSpec);

        // return을 위한 직렬화 및 Base64 인코딩
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        String encodedIv = Base64.getEncoder().encodeToString(iv);

        if (cipherText == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: ENCRYPTION FAILED");

        }else {
            Map<String, Object> result = new HashMap<>();
            result.put("SecretKey", encodedKey);
            result.put("ivParameterSpec", encodedIv);
            result.put("CipherText", cipherText);

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    // 양방향 암호화 - 대칭키 AES 복호화
    public ResponseEntity<?> decodeAES(Map<String, String> request) {

        String cipherText = request.get("Ciphertext");
        String encodedKey = request.get("SecretKey");
        String encodedIv = request.get("ivParameterSpec");

        try {
            // Base64 디코딩
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            byte[] decodedIv = Base64.getDecoder().decode(encodedIv);

            // SecretKeySpec과 IvParameterSpec 생성
            SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(decodedIv);

            // AES 복호화
            String decryptedText = decrypt(cipherText, secretKey, ivParameterSpec);
            if (decryptedText != null) {
                return ResponseEntity.status(HttpStatus.OK).body(decryptedText);
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: DECRYPTION FAILED. INVALID CIPHERTEXT");
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: DECRYPTION FAILED. INVALID SECRET KEY");
        }
    }

    // AES 암호화 메서드
    public static String encrypt(String plainText, SecretKey secretKey, IvParameterSpec ivParameterSpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        }catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    // AES 복호화 메서드
    public static String decrypt(String cipherText, SecretKey secretKey, IvParameterSpec ivParameterSpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(plainText);
        }catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    // 양방향 암호화 - 비대칭키 RSA 키 생성
    public ResponseEntity<?> createRSAKeys() {
        Map<String, String> response = new HashMap<>();
        try {
            // RSA 키 쌍 생성
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // 1024, 2048, 4096 비트 키
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 키를 Base64로 인코딩
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            // 응답에 키 추가
            response.put("publicKey", publicKeyStr);
            response.put("privateKey", privateKeyStr);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: SERVER ERROR");
        }
    }

    // 양방향 암호화 - 비대칭키 RSA 암호화
    public ResponseEntity<?> encodeRSA(String plainText, String keyStr, String keyType) {
        try {
            // Cipher 객체 생성 및 RSA 알고리즘 설정
            Cipher cipher = Cipher.getInstance("RSA");

            // 공개키 또는 비밀키를 사용하여 암호화 수행
            if (keyType.equalsIgnoreCase("public")) {
                // 공개키로 암호화
                PublicKey publicKey = getPublicKeyFromBase64(keyStr);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            } else if (keyType.equalsIgnoreCase("private")) {
                // 비밀키로 암호화
                PrivateKey privateKey = getPrivateKeyFromBase64(keyStr);
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            }

            // 평문을 암호화하고 Base64로 인코딩
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);

            // 암호화된 텍스트 반환
            return ResponseEntity.status(HttpStatus.OK).body(encryptedText);

        } catch (Exception e) {
            log.error(e.getMessage());
            // 암호화 실패 시 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: THE KEY IS NOT VALID");
        }
    }

    // 양방향 암호화 - 비대칭키 RSA 복호화
    public ResponseEntity<?> decodeRSA(String cipherText, String keyStr, String keyType) {
        try {
            // Cipher 객체 생성 및 RSA 알고리즘 설정
            Cipher cipher = Cipher.getInstance("RSA");

            // 공개키 또는 비밀키를 사용하여 복호화 수행
            if (keyType.equalsIgnoreCase("public")) {
                // 공개키로 복호화
                PublicKey publicKey = getPublicKeyFromBase64(keyStr);
                cipher.init(Cipher.DECRYPT_MODE, publicKey);

            } else if (keyType.equalsIgnoreCase("private")) {
                // 비밀키로 복호화
                PrivateKey privateKey = getPrivateKeyFromBase64(keyStr);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
            }

            // 암호문을 복호화하고 평문을 반환
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            String decryptedText = new String(decryptedBytes);

            // 복호화된 텍스트 반환
            return ResponseEntity.status(HttpStatus.OK).body(decryptedText);

        } catch (Exception e) {
            log.error(e.getMessage());
            // 복호화 실패 시 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR :: THE KEY IS NOT VALID");
        }
    }

    // Base64로 인코딩된 공개키를 PublicKey 객체로 변환
    private PublicKey getPublicKeyFromBase64(String keyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // Base64로 인코딩된 비밀키를 PrivateKey 객체로 변환
    private PrivateKey getPrivateKeyFromBase64(String keyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
