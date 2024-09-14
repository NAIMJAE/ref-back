package kr.co.reference.dataStorage;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserCart is a Querydsl query type for UserCart
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserCart extends EntityPathBase<UserCart> {

    private static final long serialVersionUID = 561170758L;

    public static final QUserCart userCart = new QUserCart("userCart");

    public final NumberPath<Integer> cartNo = createNumber("cartNo", Integer.class);

    public final StringPath prodId = createString("prodId");

    public final StringPath uid = createString("uid");

    public QUserCart(String variable) {
        super(UserCart.class, forVariable(variable));
    }

    public QUserCart(Path<? extends UserCart> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserCart(PathMetadata metadata) {
        super(UserCart.class, metadata);
    }

}

