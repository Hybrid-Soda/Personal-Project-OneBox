package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.global.util.MimeTypeResolver;
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

        // 확장자와 MIME 타입 추출
        String ext = FilenameUtils.getExtension(dto.getFileName());
        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(ext);
            dto.setContentType(mimeType);
        }

        fileValidator.validateForUpload(dto, user.getUsedQuota());  // 파일 검증
        String objectName = fileHandler.upload(dto, ext);           // 스토리지 업로드
        Metadata metadata = metadataMapper.createMetadata(          // 메타데이터 저장
                user, parentFolder, dto.getFileName(), dto.getFileSize(), objectName, dto.getContentType()
        );
        metadataRepository.save(metadata);
        user.setUsedQuota(user.getUsedQuota() + dto.getFileSize()); // 유저 저장공간 사용량 합산
    }

    public String getPreSignedUrl(UploadFileDto dto) {
        User user = findUser(dto.getUserId());
        Metadata parentFolder = findFolder(dto.getParentFolderId());

        String ext = FilenameUtils.getExtension(dto.getFileName());
        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(ext);
            dto.setContentType(mimeType);
        }

        fileValidator.validateForUpload(dto, user.getUsedQuota());
        return fileHandler.getPresignedUrl(dto, ext);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findFolder(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
