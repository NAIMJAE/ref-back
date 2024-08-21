package kr.co.reference.openApi;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QShinchan is a Querydsl query type for Shinchan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShinchan extends EntityPathBase<Shinchan> {

    private static final long serialVersionUID = 989053480L;

    public static final QShinchan shinchan = new QShinchan("shinchan");

    public final NumberPath<Integer> charAge = createNumber("charAge", Integer.class);

    public final DatePath<java.time.LocalDate> charBirth = createDate("charBirth", java.time.LocalDate.class);

    public final StringPath charBloodType = createString("charBloodType");

    public final StringPath charEtc = createString("charEtc");

    public final StringPath charGender = createString("charGender");

    public final NumberPath<Float> charHeight = createNumber("charHeight", Float.class);

    public final StringPath charImg = createString("charImg");

    public final StringPath charJob = createString("charJob");

    public final StringPath charName = createString("charName");

    public final NumberPath<Integer> charNo = createNumber("charNo", Integer.class);

    public final NumberPath<Float> charWeight = createNumber("charWeight", Float.class);

    public QShinchan(String variable) {
        super(Shinchan.class, forVariable(variable));
    }

    public QShinchan(Path<? extends Shinchan> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShinchan(PathMetadata metadata) {
        super(Shinchan.class, metadata);
    }

}

