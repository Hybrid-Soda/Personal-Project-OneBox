package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlRequest;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.UploadStatus;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.global.exception.StorageException;
import com.devnovus.oneBox.global.util.MimeTypeResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileDataServiceV3 {
    private final FileValidator fileValidator;
    private final MetadataMapper metadataMapper;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;

    /** pre-signed URL 생성 */
    @Transactional
    public PreSignedUrlResponse createPreSignedUrl(PreSignedUrlRequest request) {
        UploadFileDto dto = request.toUploadFileDto();
        Metadata metadata = createMetadataForUpload(dto);

        String uploadUrl = fileRepository.createPreSignedUploadUrl(
                metadata.getFileMetadata().getObjectName(),
                dto.getContentType()
        );

        return PreSignedUrlResponse.builder()
                .metadataId(metadata.getId())
                .objectName(metadata.getFileMetadata().getObjectName())
                .uploadUrl(uploadUrl)
                .contentType(dto.getContentType())
                .build();
    }

    /** pre-signed 업로드 완료 처리 */
    @Transactional
    public void completePreSignedUpload(Long metadataId, String eTag) {
        if (eTag == null) {
            throw new ApplicationException(ApplicationError.E_TAG_NOT_RETURNED);
        }

        Metadata metadata = findMetadata(metadataId);
        fileValidator.validateFileType(metadata.getType());

        try {
            fileRepository.verifyObjectExists(
                    metadata.getFileMetadata().getObjectName(),
                    metadata.getSize()
            );

            metadata.getFileMetadata().setUploadStatus(UploadStatus.DONE);
            metadataRepository.save(metadata);
        } catch (StorageException e) {
            compensateUploadFailure(metadata);
            throw new ApplicationException(ApplicationError.FILE_NOT_SAVED);
        }
    }

    /** 메타데이터 생성 */
    @Transactional
    public Metadata createMetadataForUpload(UploadFileDto dto) {
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

        return metadata;
    }

    @Transactional
    public void compensateUploadFailure(Metadata metadata) {
        User user = metadata.getOwner();

        user.minusUsedQuota(metadata.getSize());
        metadata.getFileMetadata().setUploadStatus(UploadStatus.FAIL);
        metadataRepository.save(metadata);
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
