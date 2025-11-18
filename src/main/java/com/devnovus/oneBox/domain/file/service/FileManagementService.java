package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.dto.MoveFileRequest;
import com.devnovus.oneBox.domain.file.dto.UpdateFileNameRequest;
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
    private final MinioFileService minioFileService;
    private final MetadataRepository metadataRepository;

    /** 파일이동 */
    @Transactional
    public void moveFile(Long fileId, MoveFileRequest req) {
        Metadata file = findMetadata(fileId);
        Metadata parentFolder = findMetadata(req.getParentFolderId());

        // 검증
        fileValidator.validateForUpdate(file.getType(), req.getParentFolderId(), file.getName());

        // 새로운 경로 생성
        String newPath = parentFolder.getPath() + file.getName();

        // 수정
        file.setPath(newPath);
        file.setParentFolder(parentFolder);
    }

    /** 파일이름수정 */
    @Transactional
    public void updateFileName(Long fileId, UpdateFileNameRequest req) {
        Metadata file = findMetadata(fileId);

        // 검증
        fileValidator.validateForUpdate(file.getType(), file.getParentFolder().getId(), req.getFileName());

        // 새로운 경로 생성
        String newPath = file.getPath().replace(file.getName(), req.getFileName());

        // 수정
        file.setPath(newPath);
        file.setName(req.getFileName());
    }

    /** 파일삭제 */
    @Transactional
    public void removeFile(Long fileId) {
        Metadata file = findMetadata(fileId);
        User owner = file.getOwner();

        fileValidator.validateFileType(file.getType());

        metadataRepository.delete(file);
        minioFileService.delete(file.getFileMetadata().getObjectName());
        owner.setUsedQuota(owner.getUsedQuota() - file.getSize());
    }

    private Metadata findMetadata(Long folderId) {
        return metadataRepository.findById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
