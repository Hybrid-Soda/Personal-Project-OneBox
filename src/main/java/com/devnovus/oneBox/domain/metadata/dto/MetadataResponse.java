package com.devnovus.oneBox.domain.metadata.dto;

import com.devnovus.oneBox.domain.metadata.entity.FileMetadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataResponse {
    private String name;
    private String path;
    private MetadataType type;
    private Long size;
    private FileMetadata fileMetadata;
}
