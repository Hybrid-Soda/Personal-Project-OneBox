package com.devnovus.oneBox.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PreSignedUrlResponse {
    private Long metadataId;
    private String objectName;
    private String uploadUrl;
    private String contentType;
}
