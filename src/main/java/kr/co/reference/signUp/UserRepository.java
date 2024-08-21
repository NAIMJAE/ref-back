package kr.co.reference.signUp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    // 인증키 검사
    public Optional<User> findByServiceKey(String serviceKey);
}
