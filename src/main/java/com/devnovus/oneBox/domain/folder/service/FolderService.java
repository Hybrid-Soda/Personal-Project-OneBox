package com.devnovus.oneBox.domain.folder.service;

import com.devnovus.oneBox.domain.folder.dto.MoveFolderRequest;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.folder.dto.CreateFolderRequest;
import com.devnovus.oneBox.domain.folder.dto.DeleteFolderRequest;
import com.devnovus.oneBox.domain.metadata.dto.MetadataResponse;
import com.devnovus.oneBox.domain.folder.dto.RenameFolderRequest;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.domain.folder.util.FolderValidator;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
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
        Metadata parentFolder = findMetadata(req.getParentFolderId());

        folderValidator.validateForCreate(parentFolder, req.getFolderName());
        metadataRepository.save(metadataMapper.createMetadata(user, parentFolder, req.getFolderName()));
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public List<MetadataResponse> getListInFolder(Long folderId) {
        Metadata folder = findMetadata(folderId);
        folderValidator.validateFolderType(folder.getType());

        return metadataRepository.findByParentFolderId(folderId)
                .stream()
                .map(metadataMapper::createMetadataResponse)
                .toList();
    }

    /** 폴더이동 */
    @Transactional
    public void moveFolder(Long folderId, MoveFolderRequest req) {
        Metadata folder = findMetadata(folderId);
        Metadata parentFolder = findMetadata(req.getParentFolderId());

        // 검증
        folderValidator.validateForMove(parentFolder, folder);

        // 상위 이동 및 경로 일괄 수정
        folder.setParentFolder(parentFolder);
        String oldPrefix = folder.getPath();
        String newPrefix = metadataMapper.genFolderPath(parentFolder.getPath(), folder.getName());
        metadataRepository.updatePathByBulk(folder.getOwner().getId(), oldPrefix, newPrefix);
    }

    /** 폴더이름수정 */
    @Transactional
    public void renameFolder(Long folderId, RenameFolderRequest req) {
        Metadata folder = findMetadata(folderId);
        Metadata parentFolder = folder.getParentFolder();

        // 검증
        folderValidator.validateForRename(parentFolder, folder, req);

        // 폴더 이름 수정 및 경로 일괄 수정
        folder.setName(req.getFolderName());
        String oldPrefix = folder.getPath();
        String newPrefix = metadataMapper.genFolderPath(parentFolder.getPath(), req.getFolderName());
        metadataRepository.updatePathByBulk(folder.getOwner().getId(), oldPrefix, newPrefix);
    }

    /** 폴더삭제 */
    @Transactional
    public void deleteFolder(Long folderId, DeleteFolderRequest req) {
        Metadata folder = findMetadata(folderId);
        folderValidator.validateFolderType(folder.getType());
        metadataRepository.deleteAllChildren(req.getUserId(), folder.getPath());
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findByIdForUpdate(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
