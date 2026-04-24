package com.ojosama.operationservice.domain.repository;

import com.ojosama.operationservice.domain.model.entity.Notice;

public interface NoticeRepository {
    Notice save(Notice notice);
}
