package com.ojosama.favoriteservice.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEventInfo is a Querydsl query type for EventInfo
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QEventInfo extends BeanPath<EventInfo> {

    private static final long serialVersionUID = -979839693L;

    public static final QEventInfo eventInfo = new QEventInfo("eventInfo");

    public final ComparablePath<java.util.UUID> eventId = createComparable("eventId", java.util.UUID.class);

    public final StringPath eventImg = createString("eventImg");

    public final StringPath eventName = createString("eventName");

    public QEventInfo(String variable) {
        super(EventInfo.class, forVariable(variable));
    }

    public QEventInfo(Path<? extends EventInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEventInfo(PathMetadata metadata) {
        super(EventInfo.class, metadata);
    }

}

