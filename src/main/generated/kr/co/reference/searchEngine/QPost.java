package kr.co.reference.searchEngine;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = 1516607782L;

    public static final QPost post = new QPost("post");

    public final StringPath contents = createString("contents");

    public final NumberPath<Integer> pNo = createNumber("pNo", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> rDate = createDateTime("rDate", java.time.LocalDateTime.class);

    public final StringPath title = createString("title");

    public final StringPath uid = createString("uid");

    public QPost(String variable) {
        super(Post.class, forVariable(variable));
    }

    public QPost(Path<? extends Post> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPost(PathMetadata metadata) {
        super(Post.class, metadata);
    }

}

