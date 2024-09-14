package kr.co.reference.searchEngine;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QKomoranIndex is a Querydsl query type for KomoranIndex
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKomoranIndex extends EntityPathBase<KomoranIndex> {

    private static final long serialVersionUID = 1439548031L;

    public static final QKomoranIndex komoranIndex = new QKomoranIndex("komoranIndex");

    public final StringPath pNoList = createString("pNoList");

    public final StringPath term = createString("term");

    public QKomoranIndex(String variable) {
        super(KomoranIndex.class, forVariable(variable));
    }

    public QKomoranIndex(Path<? extends KomoranIndex> path) {
        super(path.getType(), path.getMetadata());
    }

    public QKomoranIndex(PathMetadata metadata) {
        super(KomoranIndex.class, metadata);
    }

}

