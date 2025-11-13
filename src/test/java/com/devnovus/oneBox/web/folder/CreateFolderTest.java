package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.*;
import com.devnovus.oneBox.web.folder.dto.CreateFolderRequest;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("FolderService – 폴더 생성 테스트")
public class CreateFolderTest {
    @InjectMocks private FolderService folderService;
    @Mock private UserRepository userRepository;
    @Mock private MetadataRepository metadataRepository;

    private User user;
    private Metadata rootFolder;
    private Metadata child1Folder;
    private Metadata fileInRootFolder;

    @BeforeEach
    void setUp() {
        user = new User();
        rootFolder = new Metadata(user, null, "root", "/");
        child1Folder = new Metadata(user, rootFolder, "child1", "/child1/");
        fileInRootFolder = new Metadata(user, rootFolder, "test.txt", "/test.txt", 1024L);
    }

    @Test
    @DisplayName("동일한 이름의 폴더가 같은 경로에 존재하는 경우")
    void duplicateFolderName() {
        // given
        CreateFolderRequest request = new CreateFolderRequest(user.getId(), rootFolder.getId(), "child1");

        given(userRepository.findById(request.getUserId())).willReturn(Optional.of(user));
        given(metadataRepository.findById(request.getParentFolderId())).willReturn(Optional.of(rootFolder));
        given(metadataRepository.existsByNameAndParentFolderIdAndType(
                child1Folder.getName(), rootFolder.getId(), MetadataType.FOLDER
        )).willReturn(true);

        // when & then
        assertThatThrownBy(() -> folderService.createFolder(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.DUPLICATE_FOLDER_NAME.getMessage());
    }

    @Test
    @DisplayName("상위 디렉토리가 존재하지 않는 경우")
    void parentNotFound() {
        // given
        CreateFolderRequest request = new CreateFolderRequest(user.getId(), 999L, "child2");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(metadataRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> folderService.createFolder(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("입력한 parentFolderId가 폴더가 아닌 경우")
    void parentIsNotFolder() {
        // given
        CreateFolderRequest request = new CreateFolderRequest(user.getId(), fileInRootFolder.getId(), "child2");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(metadataRepository.findById(request.getParentFolderId())).willReturn(Optional.of(fileInRootFolder));

        // when & then
        assertThatThrownBy(() -> folderService.createFolder(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.IS_NOT_FOLDER.getMessage());
    }

    @Test
    @DisplayName("같은 폴더 내에 100개 이상의 폴더가 있는 경우")
    void tooManyFolders() {
        // given
        CreateFolderRequest request = new CreateFolderRequest(user.getId(), rootFolder.getId(), "child2");

        given(userRepository.findById(request.getUserId())).willReturn(Optional.of(user));
        given(metadataRepository.findById(request.getParentFolderId())).willReturn(Optional.of(rootFolder));
        given(metadataRepository.countByParentFolderId(rootFolder.getId())).willReturn(100L);

        // when & then
        assertThatThrownBy(() -> folderService.createFolder(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.TOO_MANY_CHILD_FOLDERS.getMessage());
    }

    @Test
    @DisplayName("폴더 경로 문자열의 길이가 255자를 넘는 경우")
    void depthExceeded() {
        // given
        String longParentPath = "/" + "A".repeat(254);
        Metadata longParentFolder = new Metadata(user, rootFolder, "longParentFolder", longParentPath);
        CreateFolderRequest request = new CreateFolderRequest(user.getId(), longParentFolder.getId(), "child2");

        given(userRepository.findById(request.getUserId())).willReturn(Optional.of(user));
        given(metadataRepository.findById(request.getParentFolderId())).willReturn(Optional.of(longParentFolder));
        given(metadataRepository.countByParentFolderId(longParentFolder.getId())).willReturn(0L);

        // when & then
        assertThatThrownBy(() -> folderService.createFolder(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.PATH_LENGTH_EXCEEDED.getMessage());
    }
}
