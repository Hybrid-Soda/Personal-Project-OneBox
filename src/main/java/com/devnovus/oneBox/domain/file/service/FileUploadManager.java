package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.dto.FileUploadRequest;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.file.util.ObjectNameGenerator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.UploadStatus;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.global.aop.lock.AdvisoryLock;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileUploadManager {
    private final FileValidator fileValidator;
    private final MetadataMapper metadataMapper;
    private final MetadataRepository metadataRepository;

    /** 메타데이터 생성 */
    @AdvisoryLock(metadataId = "#dto.parentFolderId")
    public Metadata createMetadata(FileUploadRequest dto) {
        // ready
        Metadata parent = findMetadata(dto.getParentFolderId());
        User owner = parent.getOwner();
        String objectName = ObjectNameGenerator.generate(dto, owner.getId());
        fileValidator.validateForUpload(dto, owner.getUsedQuota());
        // exec
        Metadata file = metadataMapper.createMetadata(owner, parent, objectName, dto);
        metadataRepository.save(file);
        owner.plusUsedQuota(dto.getFileSize());
        return file;
    }

    /** 파일업로드 성공시 */
    @AdvisoryLock(metadataId = "#fileId")
    public void handleUploadSuccess(Long fileId) {
        // ready
        Metadata file = findMetadata(fileId);
        // exec
        file.getFileMetadata().setUploadStatus(UploadStatus.DONE);
    }

    /** 파일업로드 실패시 */
    @AdvisoryLock(metadataId = "#fileId")
    public void handleUploadFailure(Long fileId) {
        // ready
        Metadata file = findMetadata(fileId);
        User owner = file.getOwner();
        // exec
        if (file.getFileMetadata().getUploadStatus() != UploadStatus.FAIL) {
            owner.minusUsedQuota(file.getSize());
            file.getFileMetadata().setUploadStatus(UploadStatus.FAIL);
        }
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }
}
