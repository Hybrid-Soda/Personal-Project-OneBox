package com.devnovus.oneBox.domain.metadata.entity;

import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import com.devnovus.oneBox.domain.metadata.enums.UploadStatus;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.global.aop.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "metadata")
public class Metadata extends BaseEntity {
    // 소유자
    @JoinColumn(name = "owner_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    // 상위폴더
    @JoinColumn(name = "parent_folder_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Metadata parentFolder;

    // 이름
    @Column(nullable = false)
    private String name;

    // 경로
    @Column(nullable = false)
    private String path;

    // 타입 (파일, 폴더)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MetadataType type;

    // 크기
    @Column(nullable = false)
    private Long size;

    // 타입이 파일인 경우
    @Embedded
    private FileMetadata fileMetadata;

    public Metadata() {}

    @Builder
    public Metadata(
            User owner, Metadata parentFolder, String name, String path,
            MetadataType type, Long size, String objectName, String mimeType
    ) {
        this.owner = owner;
        this.parentFolder = parentFolder;
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = (size == null ? 0L : size);

        if (objectName != null) {
            this.fileMetadata = new FileMetadata(objectName, mimeType, UploadStatus.UPLOADING);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setParentFolder(Metadata parentFolder) {
        this.parentFolder = parentFolder;
    }
}
