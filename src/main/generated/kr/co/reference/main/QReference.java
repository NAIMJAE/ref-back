package kr.co.reference.main;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReference is a Querydsl query type for Reference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReference extends EntityPathBase<Reference> {

    private static final long serialVersionUID = -1529951884L;

    public static final QReference reference = new QReference("reference");

    public final StringPath refApi = createString("refApi");

    public final StringPath refCate = createString("refCate");

    public final DatePath<java.time.LocalDate> refCreate = createDate("refCreate", java.time.LocalDate.class);

    public final StringPath refIntro = createString("refIntro");

    public final NumberPath<Integer> refNo = createNumber("refNo", Integer.class);

    public final StringPath refThumb = createString("refThumb");

    public final StringPath refTitle = createString("refTitle");

    public final DatePath<java.time.LocalDate> refUpdate = createDate("refUpdate", java.time.LocalDate.class);

    public final StringPath refVersion = createString("refVersion");

    public QReference(String variable) {
        super(Reference.class, forVariable(variable));
    }

    public QReference(Path<? extends Reference> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReference(PathMetadata metadata) {
        super(Reference.class, metadata);
    }

}

