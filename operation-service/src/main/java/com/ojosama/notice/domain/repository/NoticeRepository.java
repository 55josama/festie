package com.ojosama.notice.domain.repository;

import com.ojosama.notice.domain.model.entity.Notice;

public interface NoticeRepository {
    Notice save(Notice notice);
}
