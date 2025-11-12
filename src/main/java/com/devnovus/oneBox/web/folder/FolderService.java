package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.domain.MetadataRepository;
import com.devnovus.oneBox.web.folder.dto.CreateFolderRequest;
import com.devnovus.oneBox.web.folder.dto.UpdateFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final MetadataRepository metadataRepository;

    /** 폴더생성 */
    @Transactional
    public void createFolder(CreateFolderRequest request) {
        // pass
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public void getFolder(Long folderId) {
        // pass
    }

    /** 폴더수정 */
    @Transactional
    public void updateFolder(Long folderId, UpdateFolderRequest request) {
        // pass
    }

    /** 폴더삭제 */
    @Transactional
    public void deleteFolder(Long folderId) {
        // pass
    }
}
