package com.devnovus.oneBox.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    // 같은 경로에 동일한 이름의 폴더 확인
    Boolean existsByNameAndParentFolderIdAndType(String name, Long parentFolderId, MetadataType type);

    // 상위 폴더 내에 있는 폴더 카운트
    long countByParentFolderIdAndType(Long parentFolderId, MetadataType type);

    // 폴더 탐색
    List<Metadata> findByParentFolderId(Long parentFolderId);

    // 상위 폴더 내에 있는 파일과 폴더 카운트
    long countByParentFolderId(Long parentFolderId);
}
