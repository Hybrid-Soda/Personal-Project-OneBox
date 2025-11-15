package com.devnovus.oneBox.domain;

import com.devnovus.oneBox.aop.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "metadata")
public class Metadata extends BaseEntity {
    // 소유자
    @JoinColumn(name = "owner_id")
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private User owner;

    // 상위폴더
    @JoinColumn(name = "parent_folder_id")
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
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
    private Long size = 0L;

    // 타입이 파일인 경우
    @Embedded
    private FileMetadata fileMetadata;

    public Metadata() {}

    // 폴더 생성 시
    public Metadata(User owner, Metadata parentFolder, String name, String path) {
        this.owner = owner;
        this.parentFolder = parentFolder;
        this.name = name;
        this.path = path;
        this.type = MetadataType.FOLDER;
    }

    // 파일 생성 시
    public Metadata(User owner, Metadata parentFolder, String name, String path, Long size, String mimeType) {
        this.owner = owner;
        this.parentFolder = parentFolder;
        this.name = name;
        this.path = path;
        this.type = MetadataType.FILE;
        this.size = size;
        this.fileMetadata = new FileMetadata(mimeType, UploadStatus.UPLOADING);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentFolder(Metadata parentFolder) {
        this.parentFolder = parentFolder;
    }
}
