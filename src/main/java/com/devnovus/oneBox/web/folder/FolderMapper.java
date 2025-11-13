package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.User;
import com.devnovus.oneBox.web.folder.dto.MetadataResponse;
import org.springframework.stereotype.Component;

@Component
public class FolderMapper {

    public Metadata createMetadata(User user, Metadata parentFolder, String folderName) {
        String path = parentFolder.getPath() + folderName + "/";
        return new Metadata(user, parentFolder, folderName, path);
    }

    public MetadataResponse createMetadataResponse(Metadata metadata) {
        return MetadataResponse.builder()
                .name(metadata.getName())
                .path(metadata.getPath())
                .type(metadata.getType())
                .size(metadata.getSize())
                .fileMetadata(metadata.getFileMetadata())
                .build();
    }
}
