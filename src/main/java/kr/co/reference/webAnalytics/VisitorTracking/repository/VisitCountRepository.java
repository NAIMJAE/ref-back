package kr.co.reference.webAnalytics.VisitorTracking.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.reference.webAnalytics.VisitorTracking.VisitCount;

@Repository
public interface VisitCountRepository extends JpaRepository<VisitCount, LocalDate> {

}
