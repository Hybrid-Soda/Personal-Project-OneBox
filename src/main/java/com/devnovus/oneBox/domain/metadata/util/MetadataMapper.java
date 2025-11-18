package com.devnovus.oneBox.domain.metadata.util;

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
        String path = parentFolder.getPath() + folderName + "/";

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
            User user, Metadata parentFolder, String fileName, Long size, String objectName, String mimeType
    ) {
        String path = parentFolder.getPath() + fileName;

        return Metadata.builder()
                .owner(user)
                .parentFolder(parentFolder)
                .name(fileName)
                .path(path)
                .type(MetadataType.FILE)
                .size(size)
                .objectName(objectName)
                .mimeType(mimeType)
                .build();
    }

    // 메타데이터 조회 객체 생성
    public MetadataResponse createMetadataResponse(Metadata metadata) {
        return MetadataResponse.builder()
                .name(metadata.getName())
                .path(metadata.getPath())
                .type(metadata.getType())
                .size(metadata.getSize())
                .fileMetadata(metadata.getFileMetadata())
                .build();
    }
}
