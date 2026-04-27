package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.command.AdminChangeUserRoleCommand;
import com.ojosama.userservice.application.dto.query.AdminDetailUserQuery;
import com.ojosama.userservice.application.dto.query.AdminUserListQuery;
import com.ojosama.userservice.application.dto.result.AdminChangeUserRoleResult;
import com.ojosama.userservice.application.dto.result.AdminUserDetailResult;
import com.ojosama.userservice.application.dto.result.AdminUserListResult;
import com.ojosama.userservice.domain.model.User;
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

    @Transactional(readOnly = true)
    public AdminUserDetailResult getDetailUser(AdminDetailUserQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return new AdminUserDetailResult(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional
    public AdminChangeUserRoleResult ChangeUserRole(AdminChangeUserRoleCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        user.changeRole(command.role());

        return new AdminChangeUserRoleResult(
                user.getId(),
                user.getRole(),
                user.getUpdatedAt()
        );
    }
}
