package com.devnovus.oneBox.domain.folder.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.folder.dto.FolderCreateRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderMoveRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderRenameRequest;
import com.devnovus.oneBox.domain.folder.util.FolderValidator;
import com.devnovus.oneBox.domain.metadata.dto.MetadataResponse;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import com.devnovus.oneBox.domain.metadata.repository.AdvisoryLockRepository;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {
    private final MetadataMapper metadataMapper;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderValidator folderValidator;
    private final MetadataRepository metadataRepository;
    private final AdvisoryLockRepository advisoryLockRepository;

    /** 폴더생성 */
    @Transactional
    public Long createFolder(FolderCreateRequest req) {
        Long ownerId = findOwnerId(req.getParentFolderId());
        advisoryLockRepository.acquireTxLock(ownerId);

        User owner = userRepository.getReferenceById(ownerId);
        Metadata parentFolder = findMetadata(req.getParentFolderId());

        folderValidator.validateForCreate(parentFolder, req.getFolderName());
        return metadataRepository.save(metadataMapper.createMetadata(owner, parentFolder, req.getFolderName())).getId();
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public List<MetadataResponse> getListInFolder(Long folderId) {
        Metadata folder = findMetadata(folderId);

        folderValidator.validateFolderType(folder.getType());
        List<Metadata> metadataList = metadataRepository.findByParentFolderId(folderId);
        return metadataMapper.createMetadataResponse(metadataList);
    }

    /** 폴더이동 */
    @Transactional
    public void moveFolder(Long folderId, FolderMoveRequest req) {
        Long ownerId = findOwnerId(folderId);
        advisoryLockRepository.acquireTxLock(ownerId);

        Metadata folder = findMetadata(folderId);
        Metadata newParentFolder = findMetadata(req.getParentFolderId());

        // 검증 및 이동
        folderValidator.validateForMove(newParentFolder, folder);
        folder.setParentFolder(newParentFolder);
    }

    /** 폴더이름수정 */
    @Transactional
    public void renameFolder(Long folderId, FolderRenameRequest req) {
        Long ownerId = findOwnerId(folderId);
        advisoryLockRepository.acquireTxLock(ownerId);

        Metadata folder = findMetadata(folderId);

        // 검증 및 수정
        folderValidator.validateForRename(folder.getParentFolder(), folder, req);
        folder.setName(req.getFolderName());
    }

    /** 폴더삭제 */
    @Transactional
    public void deleteFolder(Long folderId) {
        Long ownerId = findOwnerId(folderId);
        advisoryLockRepository.acquireTxLock(ownerId);

        Metadata folder = findMetadata(folderId);
        folderValidator.validateFolderType(folder.getType());

        List<Metadata> children = metadataRepository.findAllChildrenByRecursive(folderId);
        User owner = findUser(ownerId);

        for (Metadata child: children) {
            if (child.getType() == MetadataType.FILE) {
                fileRepository.removeObject(child.getFileMetadata().getObjectName());
                owner.minusUsedQuota(child.getSize());
            }
        }

        metadataRepository.deleteAllByIds(
                children.stream().map(Metadata::getId).toList()
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }

    private Long findOwnerId(Long metadataId) {
        return metadataRepository.findOwnerIdById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }
}
