package com.ojosama.notificationservice.domain.model.emailLog;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmailLog is a Querydsl query type for EmailLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailLog extends EntityPathBase<EmailLog> {

    private static final long serialVersionUID = 1587330248L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmailLog emailLog = new QEmailLog("emailLog");

    public final com.ojosama.common.audit.QBaseUserEntity _super = new com.ojosama.common.audit.QBaseUserEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final ComparablePath<java.util.UUID> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final ComparablePath<java.util.UUID> deletedBy = _super.deletedBy;

    public final StringPath email = createString("email");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final com.ojosama.notificationservice.domain.model.notification.QNotification notification;

    public final EnumPath<Status> status = createEnum("status", Status.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final ComparablePath<java.util.UUID> updatedBy = _super.updatedBy;

    public QEmailLog(String variable) {
        this(EmailLog.class, forVariable(variable), INITS);
    }

    public QEmailLog(Path<? extends EmailLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmailLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmailLog(PathMetadata metadata, PathInits inits) {
        this(EmailLog.class, metadata, inits);
    }

    public QEmailLog(Class<? extends EmailLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notification = inits.isInitialized("notification") ? new com.ojosama.notificationservice.domain.model.notification.QNotification(forProperty("notification"), inits.get("notification")) : null;
    }

}

