package com.devnovus.oneBox.domain.user.service;

import com.devnovus.oneBox.domain.user.dto.QuotaResponse;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${storage.max-quota}")
    private long totalQuota;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public QuotaResponse getUsedQuota(Long userId) {
        User user = findUser(userId);
        return new QuotaResponse(totalQuota, user.getUsedQuota());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }
}
