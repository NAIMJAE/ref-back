package kr.co.reference.webAnalytics.VisitorTracking.repository;

import java.util.List;

import kr.co.reference.webAnalytics.VisitorTracking.VisitCount;

public interface VisitCountRepositoryCustom {

    // 일일 방문자 수 (한달)
    public List<VisitCount> selectVisitCount1Month();
    
}
