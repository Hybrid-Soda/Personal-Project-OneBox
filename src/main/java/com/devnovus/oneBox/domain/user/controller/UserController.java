package com.devnovus.oneBox.domain.user.controller;

import com.devnovus.oneBox.domain.user.dto.QuotaResponse;
import com.devnovus.oneBox.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}/quota")
    public ResponseEntity<QuotaResponse> getUsedQuota(
            @PathVariable Long userId
    ) {
        QuotaResponse response = userService.getUsedQuota(userId);
        return ResponseEntity.ok().body(response);
    }
}
