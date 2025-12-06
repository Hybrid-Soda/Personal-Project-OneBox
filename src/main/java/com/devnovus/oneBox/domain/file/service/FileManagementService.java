package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.FileMoveRequest;
import com.devnovus.oneBox.domain.file.dto.FileRenameRequest;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileManagementService {
    private final FileValidator fileValidator;
    private final FileRepository fileRepository;
    private final MetadataRepository metadataRepository;

    /** 파일이동 */
    @Transactional
    public void moveFile(Long fileId, FileMoveRequest req) {
        Metadata file = findMetadata(fileId);
        Metadata parentFolder = findMetadata(req.getParentFolderId());

        // 검증 및 이동
        fileValidator.validateForUpdate(file.getType(), req.getParentFolderId(), file.getName());
        file.setParentFolder(parentFolder);
    }

    /** 파일이름수정 */
    @Transactional
    public void updateFileName(Long fileId, FileRenameRequest req) {
        Metadata file = findMetadata(fileId);

        // 검증 및 수정
        fileValidator.validateForUpdate(file.getType(), file.getParentFolder().getId(), req.getFileName());
        file.setName(req.getFileName());
    }

    /** 파일삭제 */
    @Transactional
    public void removeFile(Long fileId) {
        Metadata file = findMetadata(fileId);
        User owner = file.getOwner();

        fileValidator.validateFileType(file.getType());

        fileRepository.removeObject(file.getFileMetadata().getObjectName());
        metadataRepository.delete(file);
        owner.minusUsedQuota(file.getSize());
    }

    private Metadata findMetadata(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }
}
