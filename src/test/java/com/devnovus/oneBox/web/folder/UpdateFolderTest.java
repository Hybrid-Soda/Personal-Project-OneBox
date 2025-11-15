package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.*;
import com.devnovus.oneBox.web.common.FolderValidator;
import com.devnovus.oneBox.web.folder.dto.UpdateFolderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FolderService – 폴더 수정 테스트")
public class UpdateFolderTest {
    @Mock private FolderMapper folderMapper;
    @Mock private UserRepository userRepository;
    @Mock private FolderValidator folderValidator;
    @Mock private MetadataRepository metadataRepository;
    @InjectMocks private FolderService folderService;

    private Metadata rootFolder;
    private Metadata folderA;
    private Metadata folderB;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        rootFolder = new Metadata(user, null, "root", "/");
        folderA = new Metadata(user, rootFolder, "A", "/A/");
        folderB = new Metadata(user, rootFolder, "LongB", "/LongB/");
    }

    @Test
    @DisplayName("자신의 하위 폴더로 이동하는 경우 예외 발생")
    void moveToChildShouldFail() {
        // given
        Long folderAId = 1L;
        UpdateFolderRequest req = new UpdateFolderRequest(user.getId(), folderAId, "A");

        given(metadataRepository.findById(folderAId)).willReturn(Optional.of(folderA));

        // when & then
        assertThatThrownBy(() -> folderService.updateFolder(folderAId, req))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.MOVE_TO_ITSELF_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("루트 폴더 수정 시 예외 발생")
    void updateRootFolderShouldFail() {
        // given
        Long rootId = 1L;
        UpdateFolderRequest req = new UpdateFolderRequest(user.getId(), rootId, "newRootName");

        given(metadataRepository.findById(rootId)).willReturn(Optional.of(rootFolder));

        // when & then
        assertThatThrownBy(() -> folderService.updateFolder(rootId, req))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.ROOT_FOLDER_UPDATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("폴더 이동 시 하위 자원의 경로 길이가 255자를 초과하면 예외 발생")
    void moveFolderShouldFailWhenChildPathExceedsLimit() {
        // given
        Long userId = 1L;
        Long folderAId = 1L;
        Long folderBId = 2L;
        String longFilePath = "/A/" + "childFile1".repeat(25) + "/"; // 총 길이 = 250
        Metadata childFile = new Metadata(user, folderA, "childFile", longFilePath);
        UpdateFolderRequest req = new UpdateFolderRequest(userId, folderBId, "A");

        given(metadataRepository.findById(folderAId)).willReturn(Optional.of(folderA));
        given(metadataRepository.findById(folderBId)).willReturn(Optional.of(folderB));
        given(metadataRepository.existsByNameAndParentFolderIdAndType(
                folderA.getName(), folderBId, MetadataType.FOLDER
        )).willReturn(false);
        given(metadataRepository.countByParentFolderIdAndType(any(), any())).willReturn(0L);
        given(metadataRepository.findLongestChildPath(userId, folderA.getPath())).willReturn(childFile.getPath());


        // when & then
        assertThatThrownBy(() -> folderService.updateFolder(folderAId, req))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.PATH_LENGTH_EXCEEDED.getMessage());

        // verify repository 호출
        verify(metadataRepository).findById(folderAId);
        verify(metadataRepository).findById(folderBId);
        verify(metadataRepository).findLongestChildPath(userId, folderA.getPath());
        verifyNoMoreInteractions(metadataRepository);
    }
}
