package com.devnovus.oneBox.web.folder.dto;

import com.devnovus.oneBox.domain.FileMetadata;
import com.devnovus.oneBox.domain.MetadataType;
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
