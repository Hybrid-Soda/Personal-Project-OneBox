package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.*;
import com.devnovus.oneBox.web.common.FolderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("FolderService – 폴더 생성 검증 테스트")
public class FolderValidatorTest {
    @Mock private MetadataRepository metadataRepository;
    @InjectMocks private FolderValidator folderValidator;

    private Metadata rootFolder;
    private Metadata childFolder;

    @BeforeEach
    void setUp() {
        User user = new User();
        rootFolder = new Metadata(user, null, "root", "/");
        childFolder = new Metadata(user, rootFolder, "child", "/child/");
    }

    @Test
    @DisplayName("메타데이터 타입 검증 테스트")
    void metadataTypeValidationTest() {
        // when & then
        assertThatThrownBy(() -> folderValidator.validateTypeFolder(MetadataType.FILE))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.NOT_A_FOLDER.getMessage());
    }

    @Test
    @DisplayName("중복된 이름 검증 테스트")
    void duplicateNameValidationTest() {
        // given
        Long parentFolderId = 1L;
        given(metadataRepository.existsByNameAndParentFolderIdAndType(
                childFolder.getName(), parentFolderId, MetadataType.FOLDER)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> folderValidator.validateFolderName(childFolder.getName(), parentFolderId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_NAME_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("하위 폴더 수 제한 검증 테스트")
    void childFolderCountLimitValidationTest() {
        // given
        Long parentFolderId = 1L;
        given(metadataRepository.countByParentFolderIdAndType(parentFolderId, MetadataType.FOLDER)).willReturn(100L);

        // when & then
        assertThatThrownBy(() -> folderValidator.validateChildFolderLimit(parentFolderId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_CHILD_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("경로 길이 제한 검증 테스트")
    void pathLengthLimitValidationTest() {
        // given
        String longParentPath = "/" + "A".repeat(254);

        // when & then
        assertThatThrownBy(() -> folderValidator.validatePathLength(longParentPath, "child2"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_PATH_LENGTH_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("순환 구조 여부 검증 테스트")
    void cyclicStructureValidationTest() {
        // given
        Long folderId = 1L;

        // when & then
        assertThatThrownBy(() -> folderValidator.validateRecursion(folderId, folderId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_CANNOT_MOVE_TO_DESCENDANT.getMessage());
    }

    @Test
    @DisplayName("루트 폴더를 수정하려는 경우")
    void updateRootFolder() {
        // when & then
        assertThatThrownBy(() -> folderValidator.validateRootFolderUpdate(rootFolder))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_NOT_ALLOWED_ROOT_MODIFY.getMessage());
    }
}
