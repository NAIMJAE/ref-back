package kr.co.reference.searchEngine;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchIndex is a Querydsl query type for SearchIndex
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchIndex extends EntityPathBase<SearchIndex> {

    private static final long serialVersionUID = 992975044L;

    public static final QSearchIndex searchIndex = new QSearchIndex("searchIndex");

    public final StringPath pNoList = createString("pNoList");

    public final StringPath term = createString("term");

    public QSearchIndex(String variable) {
        super(SearchIndex.class, forVariable(variable));
    }

    public QSearchIndex(Path<? extends SearchIndex> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchIndex(PathMetadata metadata) {
        super(SearchIndex.class, metadata);
    }

}

