package com.devnovus.oneBox.domain.metadata.repository;

import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Metadata m WHERE m.id = :id")
    Optional<Metadata> findByIdForUpdate(@Param("id") Long id);

    // 같은 경로에 동일한 이름의 폴더 확인
    Boolean existsByNameAndParentFolderIdAndType(String name, Long parentFolderId, MetadataType type);

    // 상위 폴더 내에 있는 폴더 카운트
    long countByParentFolderIdAndType(Long parentFolderId, MetadataType type);

    // 폴더 탐색
    List<Metadata> findByParentFolderId(Long parentFolderId);

    // 상위 폴더 내에 있는 파일과 폴더 카운트
    long countByParentFolderId(Long parentFolderId);

    @Modifying
    @Query(value =
            "UPDATE metadata " +
            "SET path = REPLACE(path, :oldPrefix, :newPrefix) " +
            "WHERE owner_id = :ownerId " +
            "AND path LIKE CONCAT(:oldPrefix, '%')",
            nativeQuery = true
    )
    void updatePathByBulk(
            @Param("ownerId") Long ownerId,
            @Param("oldPrefix") String oldPrefix,
            @Param("newPrefix") String newPrefix
    );

    // 최대 path 길이를 가진 자식의 path 조회
    @Query(value =
            "SELECT path FROM metadata " +
            "WHERE owner_id = :ownerId " +
            "AND path LIKE CONCAT(:oldPath, '%') " +
            "ORDER BY LENGTH(path) DESC " +
            "LIMIT 1",
            nativeQuery = true
    )
    String findLongestChildPath(@Param("ownerId") Long ownerId, @Param("oldPath") String oldPath);

    // 폴더 하위 자원 삭제
    @Modifying
    @Query(value =
            "DELETE FROM metadata " +
            "WHERE owner_id = :ownerId " +
            "AND path LIKE CONCAT(:path, '%')",
            nativeQuery = true
    )
    void deleteAllChildren(@Param("ownerId") Long ownerId, @Param("path") String path);
}
