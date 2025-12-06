package com.devnovus.oneBox.domain.folder.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.folder.dto.FolderCreateRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderMoveRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderRenameRequest;
import com.devnovus.oneBox.domain.folder.util.FolderValidator;
import com.devnovus.oneBox.domain.metadata.dto.MetadataResponse;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.global.aop.lock.AdvisoryLock;
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
    private final FileRepository fileRepository;
    private final FolderValidator folderValidator;
    private final MetadataRepository metadataRepository;

    /** 폴더생성 */
    @AdvisoryLock(metadataId = "#req.parentFolderId")
    public Long createFolder(FolderCreateRequest req) {
        // ready
        Metadata parentFolder = findMetadata(req.getParentFolderId());
        folderValidator.validateForCreate(parentFolder, req.getFolderName());
        // exec
        return metadataRepository.save(
                metadataMapper.createMetadata(parentFolder.getOwner(), parentFolder, req.getFolderName())
        ).getId();
    }

    /** 폴더조회 */
    @Transactional(readOnly = true)
    public List<MetadataResponse> getListInFolder(Long folderId) {
        // ready
        Metadata folder = findMetadata(folderId);
        folderValidator.validateFolderType(folder.getType());
        // exec
        List<Metadata> metadataList = metadataRepository.findByParentFolderId(folderId);
        return metadataMapper.createMetadataResponse(metadataList);
    }

    /** 폴더이동 */
    @AdvisoryLock(metadataId = "#folderId")
    public void moveFolder(Long folderId, FolderMoveRequest req) {
        // ready
        Metadata folder = findMetadata(folderId);
        Metadata newParentFolder = findMetadata(req.getParentFolderId());
        folderValidator.validateForMove(newParentFolder, folder);
        // exec
        folder.setParentFolder(newParentFolder);
    }

    /** 폴더이름수정 */
    @AdvisoryLock(metadataId = "#folderId")
    public void renameFolder(Long folderId, FolderRenameRequest req) {
        // ready
        Metadata folder = findMetadata(folderId);
        folderValidator.validateForRename(folder.getParentFolder(), folder, req);
        // exec
        folder.setName(req.getFolderName());
    }

    /** 폴더삭제 */
    @AdvisoryLock(metadataId = "#folderId")
    public void deleteFolder(Long folderId) {
        // ready
        Metadata folder = findMetadata(folderId);
        folderValidator.validateFolderType(folder.getType());
        // exec
        List<Metadata> children = metadataRepository.findAllChildrenByRecursive(folderId);
        for (Metadata child: children) {
            if (child.getType() == MetadataType.FILE) {
                fileRepository.removeObject(child.getFileMetadata().getObjectName());
                folder.getOwner().minusUsedQuota(child.getSize());
            }
        }
        metadataRepository.deleteAllByIds(children.stream().map(Metadata::getId).toList());
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }
}
