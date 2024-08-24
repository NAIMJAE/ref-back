package kr.co.reference.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainService {

    private final ReferenceRepository referenceRepository;

    // 메인 페이지 목록 호출
    public ResponseEntity<?> refList() {
        List<Reference> reference = referenceRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(reference);
    }
}
