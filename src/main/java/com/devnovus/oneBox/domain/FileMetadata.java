package com.devnovus.oneBox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FileMetadata {
    // 파일 MIME 파입
    @Column(name = "mime_type")
    private String mimeType;
    // 업로드 상태
    @Column(name = "upload_status")
    private String uploadStatus;
}
