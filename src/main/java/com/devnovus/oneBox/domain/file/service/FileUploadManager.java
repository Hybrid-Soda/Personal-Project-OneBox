package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.UploadStatus;
import com.devnovus.oneBox.domain.metadata.repository.AdvisoryLockRepository;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.global.util.MimeTypeResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadManager {
    private final FileValidator fileValidator;
    private final MetadataMapper metadataMapper;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;
    private final AdvisoryLockRepository advisoryLockRepository;

    /** 메타데이터 생성 */
    @Transactional
    public Long createMetadata(UploadFileDto dto) {
        advisoryLockRepository.acquireTxLock(dto.getUserId());
        // 파일 검증
        User user = findUser(dto.getUserId());
        String objectName = getObjectName(dto);
        fileValidator.validateForUpload(dto, user.getUsedQuota());

        // 메타데이터 생성
        Metadata parent = findMetadata(dto.getParentFolderId());
        Metadata metadata = metadataMapper.createMetadata(user, parent, objectName, dto);
        metadataRepository.save(metadata);

        // 유저 저장공간 반영
        user.plusUsedQuota(dto.getFileSize());

        return metadata.getId();
    }

    /** 스토리지 업로드 */
    public String uploadToStorage(UploadFileDto dto, Long fileId) {
        Metadata file = findMetadata(fileId);
        return fileRepository.save(dto, file.getFileMetadata().getObjectName());
    }

    /** 파일업로드 성공시 */
    @Transactional
    public void handleUploadSuccess(Long ownerId, Long fileId) {
        advisoryLockRepository.acquireTxLock(ownerId);

        Metadata file = findMetadata(fileId);
        file.getFileMetadata().setUploadStatus(UploadStatus.DONE);
    }

    /** 파일업로드 실패시 */
    @Transactional
    public void handleUploadFailure(Long ownerId, Long fileId) {
        advisoryLockRepository.acquireTxLock(ownerId);

        User owner = findUser(ownerId);
        Metadata file = findMetadata(fileId);

        if (file.getFileMetadata().getUploadStatus() != UploadStatus.FAIL) {
            owner.minusUsedQuota(file.getSize());
            file.getFileMetadata().setUploadStatus(UploadStatus.FAIL);
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }

    private String getObjectName(UploadFileDto dto) {
        String extension = FilenameUtils.getExtension(dto.getFileName());

        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(extension);
            dto.setContentType(mimeType);
        }

        return String.format("%d/%s.%s", dto.getUserId(), UUID.randomUUID(), extension);
    }
}
