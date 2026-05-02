package com.ojosama.notificationservice.domain.model.notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTargetInfo is a Querydsl query type for TargetInfo
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QTargetInfo extends BeanPath<TargetInfo> {

    private static final long serialVersionUID = -854887204L;

    public static final QTargetInfo targetInfo = new QTargetInfo("targetInfo");

    public final EnumPath<Target> target = createEnum("target", Target.class);

    public final ComparablePath<java.util.UUID> targetId = createComparable("targetId", java.util.UUID.class);

    public final EnumPath<TargetType> targetType = createEnum("targetType", TargetType.class);

    public QTargetInfo(String variable) {
        super(TargetInfo.class, forVariable(variable));
    }

    public QTargetInfo(Path<? extends TargetInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTargetInfo(PathMetadata metadata) {
        super(TargetInfo.class, metadata);
    }

}

