package com.devnovus.oneBox.domain;

import com.devnovus.oneBox.aop.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "metadata")
public class Metadata extends BaseEntity {
    // 소유자
    @JoinColumn(name = "owner_id", nullable = false)
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private User ownerId;

    // 상위폴더
    @JoinColumn(name = "parent_folder_id", nullable = false)
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Metadata parentFolderId;

    // 이름
    @Column(nullable = false)
    private String name;

    // 경로
    @Column(nullable = false)
    private String path;

    // 타입 (파일, 폴더)
    @Column(nullable = false)
    private MetadataType type;

    // 크기
    @Column(nullable = false)
    private Long size = 0L;

    // 타입이 파일인 경우
    @Embedded
    private FileMetadata fileMetadata;
}
