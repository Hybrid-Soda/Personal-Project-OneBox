package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.FileMoveRequest;
import com.devnovus.oneBox.domain.file.dto.FileRenameRequest;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.global.aop.lock.AdvisoryLock;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileManagementService {
    private final FileValidator fileValidator;
    private final FileRepository fileRepository;
    private final MetadataRepository metadataRepository;

    /** 파일이동 */
    @AdvisoryLock(metadataId = "#fileId")
    public void moveFile(Long fileId, FileMoveRequest req) {
        // ready
        Metadata file = findMetadata(fileId);
        Metadata newParentFolder = findMetadata(req.getParentFolderId());
        fileValidator.validateForUpdate(file.getType(), req.getParentFolderId(), file.getName());
        // exec
        file.setParentFolder(newParentFolder);
    }

    /** 파일이름수정 */
    @AdvisoryLock(metadataId = "#fileId")
    public void renameFile(Long fileId, FileRenameRequest req) {
        // ready
        Metadata file = findMetadata(fileId);
        fileValidator.validateForUpdate(file.getType(), file.getParentFolder().getId(), req.getFileName());
        // exec
        file.setName(req.getFileName());
    }

    /** 파일삭제 */
    @AdvisoryLock(metadataId = "#fileId")
    public void removeFile(Long fileId) {
        // ready
        Metadata file = findMetadata(fileId);
        fileValidator.validateFileType(file.getType());
        // exec
        fileRepository.removeObject(file.getFileMetadata().getObjectName());
        metadataRepository.delete(file);
        file.getOwner().minusUsedQuota(file.getSize());
    }

    private Metadata findMetadata(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }
}
