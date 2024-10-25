package kr.co.reference.webAnalytics.VisitorTracking.repository;

import kr.co.reference.webAnalytics.VisitorTracking.QVisitCount;
import kr.co.reference.webAnalytics.VisitorTracking.VisitCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.time.LocalDate;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Slf4j
@RequiredArgsConstructor
@Repository
public class VisitCountRepositoryImpl implements VisitCountRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QVisitCount qVisitCount = QVisitCount.visitCount1;

    // 일일 방문자 수 (한달)
    @Override
    public List<VisitCount> selectVisitCount1Month() {
        LocalDate oneMonthBefore = LocalDate.now().minusWeeks(2);

        return jpaQueryFactory.selectFrom(qVisitCount)
            .where(qVisitCount.visitDate.after(oneMonthBefore))
            .orderBy(qVisitCount.visitDate.desc())
            .fetch();

    }
}
