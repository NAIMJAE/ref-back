package kr.co.reference.searchEngine;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.reference.signUp.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QPost qPost = QPost.post;
    private final QUser qUser = QUser.user;

    // 게시글 목록 조회
    public Page<Tuple> selectPostList(Pageable pageable) {
        QueryResults<Tuple> tuple = jpaQueryFactory
                .select(qPost, qUser.name)
                .from(qPost)
                .join(qUser)
                .on(qPost.uid.eq(qUser.uid))
                .orderBy(qPost.rDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Tuple> postList = tuple.getResults();
        int total = (int) tuple.getTotal();

        return new PageImpl<>(postList, pageable, total);
    }

}
