package com.devnovus.oneBox.domain.folder.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.folder.dto.FolderCreateRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderMoveRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderRenameRequest;
import com.devnovus.oneBox.domain.folder.util.FolderValidator;
import com.devnovus.oneBox.domain.metadata.dto.MetadataResponse;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
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
        return metadataRepository.findByParentFolderId(folderId)
                .stream()
                .map(metadataMapper::createMetadataResponse)
                .toList();
    }

    /** 폴더이동 */
    @Transactional
    public void moveFolder(Long folderId, FolderMoveRequest req) {
        Long ownerId = findOwnerId(folderId);
        advisoryLockRepository.acquireTxLock(ownerId);

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
    public void renameFolder(Long folderId, FolderRenameRequest req) {
        Long ownerId = findOwnerId(folderId);
        advisoryLockRepository.acquireTxLock(ownerId);

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
    public void deleteFolder(Long folderId) {
        Long ownerId = findOwnerId(folderId);
        advisoryLockRepository.acquireTxLock(ownerId);

        Metadata folder = findMetadata(folderId);
        User user = folder.getOwner();
        folderValidator.validateFolderType(folder.getType());

        // 파일 삭제
        List<Metadata> childFiles = metadataRepository.findChildFiles(folder.getOwner().getId(), folder.getPath());

        for (Metadata file: childFiles) {
            fileRepository.removeObject(file.getFileMetadata().getObjectName());
            user.minusUsedQuota(file.getSize());
        }

        // 메타데이터 삭제
        metadataRepository.deleteAllChildren(ownerId, folder.getPath());
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
