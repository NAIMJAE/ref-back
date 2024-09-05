package kr.co.reference.searchEngine;

import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    // 게시글 목록 조회
    public Page<Tuple> selectPostList(Pageable pageable);
}
