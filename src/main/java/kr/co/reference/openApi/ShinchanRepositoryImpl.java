package kr.co.reference.openApi;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ShinchanRepositoryImpl implements ShinchanRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QShinchan qShinchan = QShinchan.shinchan;


    @Override
    public List<Shinchan> selectShinchanList(int numOfRows, int pageNo) {

        return jpaQueryFactory
                .selectFrom(qShinchan)
                .offset(numOfRows * (pageNo - 1))
                .limit(numOfRows)
                .orderBy(qShinchan.charNo.asc())
                .fetch();
    }
}
