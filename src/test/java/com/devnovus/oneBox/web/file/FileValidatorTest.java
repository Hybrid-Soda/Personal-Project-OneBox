package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 검증 테스트")
public class FileValidatorTest {
    @Mock private MetadataRepository metadataRepository;
    @InjectMocks private FileValidator fileValidator;

    @Test
    @DisplayName("메타데이터 타입 검증 테스트")
    void metadataTypeValidationTest() {
        // when & then
        assertThatThrownBy(() -> fileValidator.validateFileType(MetadataType.FOLDER))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.NOT_A_FILE.getMessage());
    }

    @Test
    @DisplayName("파일명이 null이면 예외 발생")
    void fileNameNullTest() {
        assertThatThrownBy(() -> fileValidator.validateFileName(null))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_INVALID_NAME.getMessage());
    }

    @Test
    @DisplayName("파일명에 금지 문자가 포함되면 예외 발생")
    void fileNameInvalidCharacterTest() {
        assertThatThrownBy(() -> fileValidator.validateFileName("inv:alid.txt"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_INVALID_NAME.getMessage());
    }

    @Test
    @DisplayName("파일명이 255자를 넘으면 예외 발생")
    void fileNameLengthLimitTest() {
        String longName = "A".repeat(256);
        assertThatThrownBy(() -> fileValidator.validateFileName(longName))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_INVALID_NAME.getMessage());
    }

    @Test
    @DisplayName("MIME 타입이 null이면 예외 발생")
    void mimeTypeNullTest() {
        assertThatThrownBy(() -> fileValidator.validateMimeType(null))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_UNSUPPORTED_TYPE.getMessage());
    }

    @Test
    @DisplayName("차단된 MIME 타입이면 예외 발생")
    void mimeTypeBlockedTest() {
        assertThatThrownBy(() -> fileValidator.validateMimeType("application/x-sh"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_UNSUPPORTED_TYPE.getMessage());
    }

    @Test
    @DisplayName("동일 파일명이 존재하면 예외 발생")
    void duplicateNameTest() {
        // given
        Long parentFolderId = 1L;
        given(metadataRepository.existsByNameAndParentFolderIdAndType(
                "test.txt", parentFolderId, MetadataType.FILE))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> fileValidator.validateDuplicateName(parentFolderId, "test.txt"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_NAME_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("사용자 용량이 부족하면 예외 발생")
    void storageQuotaExceededTest() {
        // given
        long usedQuota = 1_000_000L;
        long fileSize = 2_000_000L;

        // when & then
        assertThatThrownBy(() -> fileValidator.validateStorageQuota(usedQuota, fileSize))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FILE_INSUFFICIENT_STORAGE.getMessage());
    }
}
