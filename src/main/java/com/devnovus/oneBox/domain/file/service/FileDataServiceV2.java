package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.DownloadFileDto;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileDataServiceV2 {
    private final FileValidator fileValidator;
    private final FileRepository fileRepository;
    private final FileUploadManager fileUploadManager;
    private final MetadataRepository metadataRepository;

    /** 파일 업로드 */
    public void uploadFile(UploadFileDto dto) {
        Long ownerId = findOwnerIdByFolderId(dto.getParentFolderId());
        Long fileId = fileUploadManager.createMetadata(dto);

        try {
            String eTag = fileUploadManager.uploadToStorage(dto, fileId);

            if (eTag == null) {
                throw new Exception();
            }
            fileUploadManager.handleUploadSuccess(ownerId, fileId);
        } catch (Exception e) {
            fileUploadManager.handleUploadFailure(ownerId, fileId);
            throw new ApplicationException(e, ApplicationError.FILE_NOT_SAVED);
        }
    }

    /** 파일 다운로드 */
    @Transactional
    public DownloadFileDto downloadFile(Long fileId) {
        Metadata metadata = findMetadata(fileId);
        fileValidator.validateFileType(metadata.getType());

        InputStream stream = fileRepository.getObject(metadata.getFileMetadata().getObjectName());

        return new DownloadFileDto(
                metadata.getSize(), metadata.getName(), metadata.getFileMetadata().getMimeType(), stream
        );
    }

    private Long findOwnerIdByFolderId(Long folderId) {
        return metadataRepository.findOwnerIdById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
