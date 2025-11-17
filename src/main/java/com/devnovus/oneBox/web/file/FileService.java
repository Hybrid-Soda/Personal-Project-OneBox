package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.domain.User;
import com.devnovus.oneBox.domain.UserRepository;
import com.devnovus.oneBox.web.common.MinioFileHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioFileHandler fileHandler;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;

    @Transactional
    public void uploadFile(Long userId, Long parentFolderId, List<MultipartFile> files) {
        User user = findUser(userId);
        Metadata parentFolder = findFolder(parentFolderId);
        AtomicLong totSize = new AtomicLong(0);

        List<Metadata> metadataList = files.stream().map(file -> {
            String path = parentFolder.getPath() + file.getName();
            String objectName = fileHandler.upload(user.getId(), file);
            totSize.addAndGet(file.getSize());

            return new Metadata(
                    user, parentFolder, file.getName(), path,
                    file.getSize(), objectName, file.getContentType()
            );
        }).toList();

        user.setUsedQuota(user.getUsedQuota() + totSize.get());
        metadataRepository.saveAll(metadataList);
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    public Metadata findFolder(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
