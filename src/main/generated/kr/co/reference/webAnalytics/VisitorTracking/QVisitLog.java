package kr.co.reference.webAnalytics.VisitorTracking;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QVisitLog is a Querydsl query type for VisitLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVisitLog extends EntityPathBase<VisitLog> {

    private static final long serialVersionUID = -501151072L;

    public static final QVisitLog visitLog = new QVisitLog("visitLog");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath language = createString("language");

    public final StringPath referer = createString("referer");

    public final TimePath<java.time.LocalTime> time = createTime("time", java.time.LocalTime.class);

    public final StringPath vtAgent = createString("vtAgent");

    public final StringPath vtCity = createString("vtCity");

    public final StringPath vtCountry = createString("vtCountry");

    public QVisitLog(String variable) {
        super(VisitLog.class, forVariable(variable));
    }

    public QVisitLog(Path<? extends VisitLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVisitLog(PathMetadata metadata) {
        super(VisitLog.class, metadata);
    }

}

