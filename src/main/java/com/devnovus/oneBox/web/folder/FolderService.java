package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.domain.*;
import com.devnovus.oneBox.web.util.MetadataMapper;
import com.devnovus.oneBox.web.folder.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final MetadataMapper metadataMapper;
    private final UserRepository userRepository;
    private final FolderValidator folderValidator;
    private final MetadataRepository metadataRepository;

    /** 폴더생성 */
    @Transactional
    public void createFolder(CreateFolderRequest req) {
        User user = userRepository.getReferenceById(req.getUserId());
        Metadata parentFolder = findFolder(req.getParentFolderId());

        // 검증
        folderValidator.validateFolderType(parentFolder.getType());
        folderValidator.validateDuplicatedName(req.getFolderName(), req.getParentFolderId());
        folderValidator.validateChildFolderLimit(parentFolder.getId());
        folderValidator.validatePathLength(parentFolder.getPath(), req.getFolderName());

        metadataRepository.save(metadataMapper.createMetadata(user, parentFolder, req.getFolderName()));
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public List<MetadataResponse> getListInFolder(Long folderId) {
        Metadata folder = findFolder(folderId);
        folderValidator.validateFolderType(folder.getType());

        return metadataRepository.findByParentFolderId(folderId)
                .stream()
                .map(metadataMapper::createMetadataResponse)
                .toList();
    }

    /** 폴더수정 */
    @Transactional
    public void updateFolder(Long folderId, UpdateFolderRequest req) {
        Metadata folder = findFolder(folderId);
        Metadata parentFolder = findFolder(req.getParentFolderId());

        // 검증
        folderValidator.validateFolderType(parentFolder.getType());
        folderValidator.validateRootFolderUpdate(folder);
        folderValidator.validateNoCircularMove(req.getParentFolderId(), folderId);
        folderValidator.validateDuplicatedName(req.getFolderName(), req.getParentFolderId());
        folderValidator.validateChildFolderLimit(parentFolder.getId());
        folderValidator.validatePathLengthForUpdate(req.getUserId(), parentFolder.getPath(), folder.getPath(), folder.getName());

        // 이름과 상위 폴더 수정
        folder.setName(req.getFolderName());
        folder.setParentFolder(parentFolder);

        // 폴더와 하위 자원들의 경로 수정
        String newPrefix = parentFolder.getPath() + folder.getName() + "/";
        metadataRepository.updatePathByBulk(req.getUserId(), folder.getPath(), newPrefix);
    }

    /** 폴더삭제 */
    @Transactional
    public void deleteFolder(Long folderId, DeleteFolderRequest req) {
        Metadata folder = findFolder(folderId);
        folderValidator.validateFolderType(folder.getType());
        metadataRepository.deleteAllChildren(req.getUserId(), folder.getPath());
    }

    public Metadata findFolder(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
