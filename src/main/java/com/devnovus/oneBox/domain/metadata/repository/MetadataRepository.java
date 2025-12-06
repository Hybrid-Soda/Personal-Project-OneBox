package com.devnovus.oneBox.domain.metadata.repository;

import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    // 소유자 식별키 조회
    @Query("SELECT m.owner.id FROM Metadata m WHERE m.id = :id")
    Optional<Long> findOwnerIdById(Long id);

    // 같은 경로에 동일한 이름의 폴더 확인
    Boolean existsByNameAndParentFolderIdAndType(String name, Long parentFolderId, MetadataType type);

    // 상위 폴더 내에 있는 폴더 카운트
    long countByParentFolderIdAndType(Long parentFolderId, MetadataType type);

    // 폴더 탐색
    List<Metadata> findByParentFolderId(Long parentFolderId);

    // 하위 폴더 CTE 조회
    @Query(value =
            "WITH RECURSIVE metadata_tree AS (" +
            " SELECT * FROM metadata WHERE id = :folderId" +
            " UNION ALL" +
            " SELECT m.* FROM metadata m" +
            " INNER JOIN metadata_tree t ON m.parent_folder_id = t.id " +
            ") " +
            "SELECT * FROM metadata_tree " +
            "ORDER BY id DESC",
            nativeQuery = true
    )
    List<Metadata> findAllChildrenByRecursive(@Param("folderId") Long folderId);

    // batch 삭제 처리
    @Modifying
    @Query("DELETE FROM Metadata m WHERE m.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
