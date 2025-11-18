package com.devnovus.oneBox.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class QuotaResponse {
    private Long totalQuota;
    private Long usedQuota;
}
