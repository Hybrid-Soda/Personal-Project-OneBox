package com.devnovus.oneBox.domain.user.entity;

import com.devnovus.oneBox.global.aop.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {
    // 현재 사용량 (단위: Byte, 기본값: 0B)
    @Column(name = "used_quota", updatable = false)
    private Long usedQuota = 0L;

    // 현재 사용량 수정
    public void setUsedQuota(Long usedQuota) {
        this.usedQuota = usedQuota;
    }
}
