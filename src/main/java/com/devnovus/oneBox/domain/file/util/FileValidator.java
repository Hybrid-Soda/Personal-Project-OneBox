package com.devnovus.oneBox.domain.file.util;

import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import com.devnovus.oneBox.domain.file.dto.FileUploadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.devnovus.oneBox.global.constant.CommonConstant.*;

@Component
@RequiredArgsConstructor
public class FileValidator {
    @Value("${storage.max-quota}")
    private long totalQuota;

    private final MetadataRepository metadataRepository;

    /** 업로드 파일 검증 */
    public void validateForUpload(FileUploadDto dto, Long usedQuota) {
        validateFileName(dto.getFileName());
        validateMimeType(dto.getContentType());
        validateDuplicateName(dto.getParentFolderId(), dto.getFileName());
        validateStorageQuota(usedQuota, dto.getFileSize());
    }

    /** 수정 파일 검증 */
    public void validateForUpdate(MetadataType type, Long parentFolderId, String fileName) {
        validateFileType(type);
        validateDuplicateName(parentFolderId, fileName);
    }

    /** 메타데이터 타입 검증 */
    public void validateFileType(MetadataType type) {
        if (type != MetadataType.FILE) throw new ApplicationException(ApplicationError.NOT_A_FILE);
    }

    /** 파일명 규칙 검증 */
    public void validateFileName(String fileName) {
        // 파일명이 없음
        if (fileName == null || fileName.isBlank()) {
            throw new ApplicationException(ApplicationError.FILE_INVALID_NAME);
        }

        // 길이 제한
        if (fileName.length() > MAX_FILE_NAME_LENGTH) {
            throw new ApplicationException(ApplicationError.FILE_INVALID_NAME);
        }

        // 금지 문자 검증
        if (INVALID_NAME_PATTERN.matcher(fileName).find()) {
            throw new ApplicationException(ApplicationError.FILE_INVALID_NAME);
        }
    }

    /** MIME 타입 검증 */
    public void validateMimeType(String contentType) {
        if (contentType == null) {
            throw new ApplicationException(ApplicationError.FILE_UNSUPPORTED_TYPE);
        }

        if (BLOCKED_MIME_TYPES.contains(contentType)) {
            throw new ApplicationException(ApplicationError.FILE_UNSUPPORTED_TYPE);
        }
    }

    /** 동일한 이름의 파일 존재 여부 검증 */
    public void validateDuplicateName(Long parentFolderId, String fileName) {
        boolean exists = metadataRepository.existsByNameAndParentFolderIdAndType(
                fileName, parentFolderId, MetadataType.FILE
        );

        if (exists) {
            throw new ApplicationException(ApplicationError.FILE_NAME_DUPLICATED);
        }
    }

    /** 사용자 잔여 용량 검증 */
    public void validateStorageQuota(Long usedQuota, long fileSize) {
        long remain = totalQuota - usedQuota;

        if (fileSize > remain) {
            throw new ApplicationException(ApplicationError.FILE_INSUFFICIENT_STORAGE);
        }
    }
}
