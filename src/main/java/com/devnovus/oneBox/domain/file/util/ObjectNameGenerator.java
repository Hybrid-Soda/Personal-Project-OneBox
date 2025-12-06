package com.devnovus.oneBox.domain.file.util;

import com.devnovus.oneBox.domain.file.dto.FileUploadRequest;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.global.util.MimeTypeResolver;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ObjectNameGenerator {
    public static String generate(FileUploadRequest dto, Long ownerId) {
        String extension = FilenameUtils.getExtension(dto.getFileName());

        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(extension);
            dto.setContentType(mimeType);
        }

        return String.format("%d/%s.%s", ownerId, UUID.randomUUID(), extension);
    }
}
