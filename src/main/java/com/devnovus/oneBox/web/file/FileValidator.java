package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.domain.MetadataType;
import com.devnovus.oneBox.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@RequiredArgsConstructor
public class FileValidator {
    @Value("${storage.max-quota}")
    private long TOTAL_QUOTA;

    private final MetadataRepository metadataRepository;

    /** 메타데이터 타입 검증 */
    public void validateFileType(MetadataType type) {
        if (type != MetadataType.FILE) throw new ApplicationException(ApplicationError.NOT_A_FILE);
    }

    /** 파일명 규칙 검증 */
    public void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ApplicationException(ApplicationError.FILE_INVALID_NAME);
        }

        // 길이 제한
        if (fileName.length() >= 255) {
            throw new ApplicationException(ApplicationError.FILE_INVALID_NAME);
        }

        // 금지 문자 검증
        if (fileName.matches(".*[\\\\/:*?\"<>|].*")) {  // 특수문자 포함 여부
            throw new ApplicationException(ApplicationError.FILE_INVALID_NAME);
        }
    }

    /** MIME 타입 검증 */
    public void validateMimeType(String contentType) {
        if (contentType == null) {
            throw new ApplicationException(ApplicationError.FILE_UNSUPPORTED_TYPE);
        }

        // 예: 실행 파일, 스크립트 파일 차단
        if (
                contentType.equals("application/x-msdownload") ||  // exe
                contentType.equals("application/x-sh")             // sh
        ) {
            throw new ApplicationException(ApplicationError.FILE_UNSUPPORTED_TYPE);
        }
    }

    /** 동일한 이름의 파일 존재 여부 검증 */
    public void validateDuplicateName(Long parentFolderId, String fileName) {
        boolean exists = metadataRepository.existsByNameAndParentFolderIdAndType(fileName, parentFolderId, MetadataType.FILE);

        if (exists) {
            throw new ApplicationException(ApplicationError.FILE_NAME_DUPLICATED);
        }
    }

    /** 사용자 잔여 용량 검증 */
    public void validateStorageQuota(User user, long fileSize) {
        long used = user.getUsedQuota();
        long remain = TOTAL_QUOTA - used;

        if (fileSize > remain) {
            throw new ApplicationException(ApplicationError.FILE_INSUFFICIENT_STORAGE);
        }
    }
}
