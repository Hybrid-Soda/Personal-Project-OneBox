package com.devnovus.oneBox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class FileMetadata {
    // 파일 MIME 파입
    @Column(name = "mime_type")
    @Enumerated(EnumType.STRING)
    private MimeType mimeType;

    // 업로드 상태
    @Column(name = "upload_status")
    @Enumerated(EnumType.STRING)
    private UploadStatus uploadStatus;
}
