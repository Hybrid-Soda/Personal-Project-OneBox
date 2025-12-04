package com.devnovus.oneBox.domain.metadata.util;

import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.metadata.dto.MetadataResponse;
import org.springframework.stereotype.Component;

@Component
public class MetadataMapper {
    // 폴더 메타데이터 엔티티 생성
    public Metadata createMetadata(
            User user, Metadata parentFolder, String folderName
    ) {
        String path = genFolderPath(parentFolder.getPath(), folderName);

        return Metadata.builder()
                .owner(user)
                .parentFolder(parentFolder)
                .name(folderName)
                .path(path)
                .type(MetadataType.FOLDER)
                .build();
    }

    // 파일 메타데이터 엔티티 생성
    public Metadata createMetadata(
            User user, Metadata parentFolder, String objectName, UploadFileDto dto
    ) {
        String path = parentFolder.getPath() + dto.getFileName();

        return Metadata.builder()
                .owner(user)
                .parentFolder(parentFolder)
                .name(dto.getFileName())
                .path(path)
                .type(MetadataType.FILE)
                .size(dto.getFileSize())
                .objectName(objectName)
                .mimeType(dto.getContentType())
                .build();
    }

    // 메타데이터 조회 객체 생성
    public MetadataResponse createMetadataResponse(Metadata metadata) {
        return MetadataResponse.builder()
                .id(metadata.getId())
                .name(metadata.getName())
                .path(metadata.getPath())
                .type(metadata.getType())
                .size(metadata.getSize())
                .fileMetadata(metadata.getFileMetadata())
                .createdAt(metadata.getCreatedAt())
                .updatedAt(metadata.getUpdatedAt())
                .build();
    }

    public PreSignedUrlResponse createPreSignedUrlResponse(Metadata metadata, String url) {
        return PreSignedUrlResponse.builder()
                .metadataId(metadata.getId())
                .uploadUrl(url)
                .build();
    }

    public String genFolderPath(String parentPath, String name) {
       return parentPath + name + "/";
    }
}
