package com.devnovus.oneBox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Embeddable
@AllArgsConstructor
public class FileMetadata {
    // 파일 MIME 파입
    @Column(name = "mime_type")
    private String mimeType;

    // 업로드 상태
    @Column(name = "upload_status")
    @Enumerated(EnumType.STRING)
    private UploadStatus uploadStatus;

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
