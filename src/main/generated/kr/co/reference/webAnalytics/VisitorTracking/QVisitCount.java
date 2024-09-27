package kr.co.reference.webAnalytics.VisitorTracking;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QVisitCount is a Querydsl query type for VisitCount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVisitCount extends EntityPathBase<VisitCount> {

    private static final long serialVersionUID = -578137749L;

    public static final QVisitCount visitCount1 = new QVisitCount("visitCount1");

    public final NumberPath<Integer> visitCount = createNumber("visitCount", Integer.class);

    public final DatePath<java.time.LocalDate> visitDate = createDate("visitDate", java.time.LocalDate.class);

    public QVisitCount(String variable) {
        super(VisitCount.class, forVariable(variable));
    }

    public QVisitCount(Path<? extends VisitCount> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVisitCount(PathMetadata metadata) {
        super(VisitCount.class, metadata);
    }

}

