package kr.co.reference.webAnalytics.VisitorTracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.reference.webAnalytics.VisitorTracking.VisitLog;

@Repository
public interface VisitLogRepository extends JpaRepository<VisitLog, Integer> {
}
