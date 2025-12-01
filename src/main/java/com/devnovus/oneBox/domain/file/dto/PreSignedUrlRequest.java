package com.devnovus.oneBox.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PreSignedUrlRequest {
    private Long userId;
    private Long parentFolderId;
    private Long fileSize;
    private String fileName;
    private String contentType;

    public UploadFileDto toUploadFileDto() {
        return new UploadFileDto(userId, parentFolderId, fileSize, fileName, contentType, null);
    }
}
