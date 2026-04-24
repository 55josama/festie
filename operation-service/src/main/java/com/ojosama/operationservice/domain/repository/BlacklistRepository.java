package com.ojosama.operationservice.domain.repository;

import com.ojosama.operationservice.domain.model.entity.Blacklist;

public interface BlacklistRepository {
    Blacklist save(Blacklist blacklist);
}
