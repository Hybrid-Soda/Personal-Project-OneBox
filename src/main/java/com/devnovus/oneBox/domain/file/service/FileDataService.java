package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.FileDownloadDto;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlRequest;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.dto.FileUploadDto;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileDataService {
    private final FileValidator fileValidator;
    private final FileRepository fileRepository;
    private final MetadataMapper metadataMapper;
    private final FileUploadManager fileUploadManager;
    private final MetadataRepository metadataRepository;

    /** 파일 업로드 */
    public void uploadFile(FileUploadDto dto) {
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

    /** pre-signed URL 생성 */
    public PreSignedUrlResponse createPreSignedUrl(PreSignedUrlRequest request) {
        FileUploadDto dto = request.toUploadFileDto();
        Long fileId = fileUploadManager.createMetadata(dto);
        Metadata file = findMetadata(fileId);

        try {
            String uploadUrl = fileRepository.getPreSignedObjectUrl(
                    file.getFileMetadata().getObjectName(), dto.getContentType()
            );
            return metadataMapper.createPreSignedUrlResponse(file, uploadUrl);
        } catch (Exception e) {
            Long ownerId = findOwnerIdByFolderId(dto.getParentFolderId());
            fileUploadManager.handleUploadFailure(ownerId, fileId);
            throw new ApplicationException(ApplicationError.FAIL_TO_GET_URL);
        }
    }

    /** 업로드 완료 */
    public void completeUpload(Long fileId, String eTag) {
        if (eTag == null) {
            throw new ApplicationException(ApplicationError.E_TAG_NOT_RETURNED);
        }

        Metadata file = findMetadata(fileId);
        Long ownerId = findOwnerIdByFolderId(file.getOwner().getId());

        try {
            fileRepository.statObject(file.getFileMetadata().getObjectName());
            fileUploadManager.handleUploadSuccess(ownerId, fileId);
        } catch (Exception e) {
            fileUploadManager.handleUploadFailure(ownerId, fileId);
            throw new ApplicationException(ApplicationError.FAIL_TO_COMPLETE_UPLOAD);
        }
    }

    /** 파일 다운로드 */
    @Transactional
    public FileDownloadDto downloadFile(Long fileId) {
        Metadata metadata = findMetadata(fileId);
        fileValidator.validateFileType(metadata.getType());

        InputStream stream = fileRepository.getObject(metadata.getFileMetadata().getObjectName());

        return new FileDownloadDto(
                metadata.getSize(), metadata.getName(), metadata.getFileMetadata().getMimeType(), stream
        );
    }

    private Long findOwnerIdByFolderId(Long folderId) {
        return metadataRepository.findOwnerIdById(folderId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }
}
