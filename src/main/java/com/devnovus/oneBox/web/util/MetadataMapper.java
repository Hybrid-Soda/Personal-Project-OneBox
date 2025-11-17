package com.devnovus.oneBox.web.util;

import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.User;
import com.devnovus.oneBox.web.folder.dto.MetadataResponse;
import org.springframework.stereotype.Component;

@Component
public class MetadataMapper {
    // 폴더 메타데이터 엔티티 생성
    public Metadata createMetadata(
            User user, Metadata parentFolder, String folderName
    ) {
        String path = parentFolder.getPath() + folderName + "/";
        return new Metadata(user, parentFolder, folderName, path);
    }

    // 파일 메타데이터 엔티티 생성
    public Metadata createMetadata(
            User user, Metadata parentFolder, String fileName, Long size, String objectName, String mimeType
    ) {
        String path = parentFolder.getPath() + fileName;
        return new Metadata(user, parentFolder, fileName, path, size, objectName, mimeType);
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
