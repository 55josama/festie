package com.ojosama.notificationservice.domain.repository;

import com.ojosama.notificationservice.domain.model.emailLog.EmailLog;

public interface EmailLogRepository {

    EmailLog save(EmailLog emailLog);
}
