package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.domain.User;
import com.devnovus.oneBox.domain.UserRepository;
import com.devnovus.oneBox.web.util.MetadataMapper;
import com.devnovus.oneBox.web.common.MinioFileHandler;
import com.devnovus.oneBox.web.file.dto.UploadFileDto;
import com.devnovus.oneBox.web.util.MimeTypeResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MetadataMapper metadataMapper;
    private final MinioFileHandler fileHandler;
    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final MetadataRepository metadataRepository;

    @Transactional
    public void uploadFile(UploadFileDto dto) {
        User user = findUser(dto.getUserId());
        Metadata parentFolder = findFolder(dto.getParentFolderId());

        // MinIO 업로드
        String ext = FilenameUtils.getExtension(dto.getFileName());
        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(ext);
            dto.setContentType(mimeType);
        }
        String objectName = fileHandler.upload(dto, ext);

        // 메타데이터 저장
        Metadata metadata = metadataMapper.createMetadata(
                user, parentFolder, dto.getFileName(), dto.getFileSize(), objectName, dto.getContentType()
        );
        metadataRepository.save(metadata);

        // 유저 저장공간 사용량 합산
        user.setUsedQuota(user.getUsedQuota() + dto.getFileSize());
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
