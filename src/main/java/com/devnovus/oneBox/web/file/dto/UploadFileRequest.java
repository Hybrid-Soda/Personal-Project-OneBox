package com.devnovus.oneBox.web.file.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UploadFileRequest {
    private Long userId;
    private Long parentFolderId;
}
