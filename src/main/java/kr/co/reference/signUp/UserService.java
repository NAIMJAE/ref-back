package kr.co.reference.signUp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public ResponseEntity<?> signUp(UserDTO userDTO) {

        String encoded = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encoded);
        userDTO.setRole("USER");

        User user = userRepository.save(modelMapper.map(userDTO, User.class));

        if (user.getRdate() != null) {
            return ResponseEntity.status(HttpStatus.OK).body(1);
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(0);
        }
    }

    // 아이디 중복 확인
    public ResponseEntity<?> checkUid(String uid) {
        Optional<User> optUser = userRepository.findById(uid);

        if (optUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(0);
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(1);
        }
    }
    
    // 로그인


}
