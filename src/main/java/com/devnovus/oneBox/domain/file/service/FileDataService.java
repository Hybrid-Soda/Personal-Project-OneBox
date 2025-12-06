package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.Repository.FileRepository;
import com.devnovus.oneBox.domain.file.dto.FileDownloadResponse;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.dto.FileUploadRequest;
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
    public void uploadFile(FileUploadRequest dto) {
        Metadata file = fileUploadManager.createMetadata(dto);

        try {
            String eTag = fileRepository.putObject(dto, file.getFileMetadata().getObjectName());
            checkETag(eTag);
            fileUploadManager.handleUploadSuccess(file.getId());

        } catch (Exception e) {
            fileUploadManager.handleUploadFailure(file.getId());
            throw new ApplicationException(e, ApplicationError.FILE_NOT_SAVED);
        }
    }

    /** pre-signed URL 생성 */
    public PreSignedUrlResponse createPreSignedUrl(FileUploadRequest req) {
        Metadata file = fileUploadManager.createMetadata(req);

        try {
            String url = fileRepository.getPreSignedObjectUrl(file.getFileMetadata().getObjectName(), req.getContentType());
            return metadataMapper.createPreSignedUrlResponse(file, url);

        } catch (Exception e) {
            fileUploadManager.handleUploadFailure(file.getId());
            throw new ApplicationException(ApplicationError.FAIL_TO_GET_URL);
        }
    }

    /** 업로드 완료 */
    public void completeUpload(Long fileId, String eTag) {
        checkETag(eTag);
        Metadata file = findMetadata(fileId);

        try {
            fileRepository.statObject(file.getFileMetadata().getObjectName());
            fileUploadManager.handleUploadSuccess(fileId);

        } catch (Exception e) {
            fileUploadManager.handleUploadFailure(fileId);
            throw new ApplicationException(ApplicationError.FAIL_TO_COMPLETE_UPLOAD);
        }
    }

    /** 파일 다운로드 */
    @Transactional(readOnly = true)
    public FileDownloadResponse downloadFile(Long fileId) {
        Metadata file = findMetadata(fileId);
        fileValidator.validateFileType(file.getType());

        InputStream stream = fileRepository.getObject(file.getFileMetadata().getObjectName());
        return new FileDownloadResponse(
                file.getSize(), file.getName(), file.getFileMetadata().getMimeType(), stream
        );
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.METADATA_NOT_FOUND));
    }

    private void checkETag(String eTag) {
        if (eTag == null || eTag.isBlank()) {
            throw new ApplicationException(ApplicationError.E_TAG_NOT_FOUND);
        }
    }
}
