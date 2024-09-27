package kr.co.reference.webAnalytics.VisitorTracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface VisitCountRepository extends JpaRepository<VisitCount, LocalDate> {

}
