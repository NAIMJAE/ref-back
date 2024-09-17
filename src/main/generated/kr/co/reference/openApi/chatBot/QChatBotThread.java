package kr.co.reference.openApi.chatBot;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChatBotThread is a Querydsl query type for ChatBotThread
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatBotThread extends EntityPathBase<ChatBotThread> {

    private static final long serialVersionUID = 809060798L;

    public static final QChatBotThread chatBotThread = new QChatBotThread("chatBotThread");

    public final StringPath assistantId = createString("assistantId");

    public final StringPath threadId = createString("threadId");

    public final StringPath uid = createString("uid");

    public QChatBotThread(String variable) {
        super(ChatBotThread.class, forVariable(variable));
    }

    public QChatBotThread(Path<? extends ChatBotThread> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatBotThread(PathMetadata metadata) {
        super(ChatBotThread.class, metadata);
    }

}

