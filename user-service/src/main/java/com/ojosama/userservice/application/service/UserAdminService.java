package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.query.AdminUserListQuery;
import com.ojosama.userservice.application.dto.result.AdminUserListResult;
import com.ojosama.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserListResult> getUsers(AdminUserListQuery query) {
        return userRepository.findAllByDeletedAtIsNull(
                        PageRequest.of(query.page(), query.size())
                )
                .map(AdminUserListResult::from);
    }
}
