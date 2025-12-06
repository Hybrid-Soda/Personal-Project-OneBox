package com.devnovus.oneBox.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@Getter
@AllArgsConstructor
public class FileDownloadResponse {
    private Long fileSize;
    private String fileName;
    private String mimeType;
    private InputStream inputStream;
}
