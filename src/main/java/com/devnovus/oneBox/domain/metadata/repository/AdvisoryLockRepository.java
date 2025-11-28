package com.devnovus.oneBox.domain.metadata.repository;

import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvisoryLockRepository extends JpaRepository<Metadata, Long> {
    @Query(value = "SELECT pg_advisory_xact_lock(:lockKey)", nativeQuery = true)
    void acquireTxLock(@Param("lockKey") Long lockKey);
}
