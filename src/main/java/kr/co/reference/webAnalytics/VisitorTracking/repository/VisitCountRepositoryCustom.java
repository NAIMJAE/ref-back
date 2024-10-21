package kr.co.reference.webAnalytics.VisitorTracking.repository;

import java.util.List;

import kr.co.reference.webAnalytics.VisitorTracking.VisitCount;

public interface VisitCountRepositoryCustom {

    // 일일 방문자 수 (2주)
    List<VisitCount> selectVisitCount2Weeks();

    // 방문자 수 합 (오늘, 1주, 한달)
    
}
