package kr.co.reference.webAnalytics.VisitorTracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitLogRepository extends JpaRepository<VisitLog, Integer> {
}
