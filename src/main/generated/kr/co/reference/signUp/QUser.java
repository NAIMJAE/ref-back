package kr.co.reference.signUp;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -551279773L;

    public static final QUser user = new QUser("user");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final DatePath<java.time.LocalDate> rdate = createDate("rdate", java.time.LocalDate.class);

    public final StringPath role = createString("role");

    public final StringPath serviceKey = createString("serviceKey");

    public final StringPath uid = createString("uid");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

