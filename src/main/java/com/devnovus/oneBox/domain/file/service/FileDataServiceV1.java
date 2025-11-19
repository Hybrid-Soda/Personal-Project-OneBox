package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.DownloadFileDto;
import com.devnovus.oneBox.domain.metadata.enums.UploadStatus;
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

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileDataServiceV1 {
    private final FileValidator fileValidator;
    private final MetadataMapper metadataMapper;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;

    /** 파일업로드 */
    @Transactional
    public void uploadFile(UploadFileDto dto) {
        // 파일 검증
        User user = findUser(dto.getUserId());
        String objectName = getObjectName(dto);
        fileValidator.validateForUpload(dto, user.getUsedQuota());

        // 메타데이터 생성
        Metadata parentFolder = findMetadata(dto.getParentFolderId());
        Metadata metadata = metadataMapper.createMetadata(user, parentFolder, objectName, dto);
        metadataRepository.save(metadata);

        // 유저 저장공간 반영
        user.plusUsedQuota(dto.getFileSize());

        // 스토리지 업로드
        String eTag = fileRepository.save(dto, objectName);

        // 스토리지 업로드 오류 시 보상 트랜잭션 작동
        if (eTag == null || eTag.isBlank()) {
            user.minusUsedQuota(dto.getFileSize());
            metadata.getFileMetadata().setUploadStatus(UploadStatus.FAIL);
            throw new ApplicationException(ApplicationError.E_TAG_NOT_RETURNED);
        }

        metadata.getFileMetadata().setUploadStatus(UploadStatus.DONE);
    }

    @Transactional
    public DownloadFileDto downloadFile(Long fileId) {
        Metadata metadata = findMetadata(fileId);
        fileValidator.validateFileType(metadata.getType());

        InputStream stream = fileRepository.download(metadata.getFileMetadata().getObjectName());

        return new DownloadFileDto(metadata.getSize(), metadata.getName(), metadata.getFileMetadata().getMimeType(), stream);
    }

    private String getObjectName(UploadFileDto dto) {
        String extension = FilenameUtils.getExtension(dto.getFileName());

        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(extension);
            dto.setContentType(mimeType);
        }

        return String.format("%d/%s.%s", dto.getUserId(), UUID.randomUUID(), extension);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
