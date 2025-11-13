package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.Metadata;
import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.domain.User;
import com.devnovus.oneBox.domain.UserRepository;
import com.devnovus.oneBox.web.folder.dto.MetadataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("FolderService – 폴더 조회 테스트")
public class GetListInFolderTest {
    @InjectMocks
    private FolderService folderService;
    @Mock private FolderMapper folderMapper;
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
    @DisplayName("folderId의 폴더가 존재하지 않는 경우")
    void parentNotFound() {
        // given
        Long folderId = 999L;
        given(metadataRepository.findById(folderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> folderService.getListInFolder(folderId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.FOLDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("folderId가 폴더의 식별자가 아닌 경우")
    void parentIsNotFolder() {
        // given
        given(metadataRepository.findById(fileInRootFolder.getId())).willReturn(Optional.of(fileInRootFolder));

        // when & then
        assertThatThrownBy(() -> folderService.getListInFolder(fileInRootFolder.getId()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ApplicationError.IS_NOT_FOLDER.getMessage());
    }

    @Test
    @DisplayName("폴더 탐색 성공")
    void getListSuccess() {
        // given
        given(metadataRepository.findById(rootFolder.getId())).willReturn(Optional.of(rootFolder));
        given(metadataRepository.findByParentFolderId(rootFolder.getId())).willReturn(List.of(child1Folder, fileInRootFolder));

        // when
        List<MetadataResponse> result = folderService.getListInFolder(rootFolder.getId());

        assertThat(result).hasSize(2);
    }
}
