package kr.co.reference.openApi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShinchanRepository extends JpaRepository<Shinchan,Integer>, ShinchanRepositoryCustom {
    // 이름으로 데이터 조회
    public Optional<Shinchan> findByCharName(String name);

}
