package com.devnovus.oneBox.domain.metadata.dto;

import com.devnovus.oneBox.domain.metadata.entity.FileMetadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataResponse {
    private Long id;
    private String name;
    private String path;
    private MetadataType type;
    private Long size;
    private FileMetadata fileMetadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
