package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.domain.User;
import com.devnovus.oneBox.domain.UserRepository;
import com.devnovus.oneBox.web.file.dto.UploadFileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;

    @Transactional
    public void uploadFile(UploadFileRequest req, List<MultipartFile> files) {
        User user = userRepository.getReferenceById(req.getUserId());
        Metadata parentFolder = findFolder(req.getParentFolderId());

        files.forEach(file -> {
            String path = parentFolder.getPath() + file.getName();
            Metadata metadata = new Metadata(user, parentFolder, file.getName(), path, file.getSize(), file.getContentType());

            metadataRepository.save(metadata);
        });
    }

    public Metadata findFolder(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
