package com.devnovus.oneBox.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
    private Long userId;
    private Long parentFolderId;
    private Long fileSize;
    private String fileName;
    private String contentType;
    private InputStream inputStream;

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
