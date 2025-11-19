package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.dto.DownloadFileDto;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import com.devnovus.oneBox.domain.file.util.FileValidator;
import com.devnovus.oneBox.domain.metadata.util.MetadataMapper;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.global.util.MimeTypeResolver;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileDataService {
    private final MetadataMapper metadataMapper;
    private final MinioFileService minioFileService;
    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final MetadataRepository metadataRepository;

    /** 파일업로드 */
    @Transactional
    public void uploadFile(UploadFileDto dto) {
        User user = findUser(dto.getUserId());
        Metadata parentFolder = findMetadata(dto.getParentFolderId());

        // 확장자와 MIME 타입 추출
        String ext = FilenameUtils.getExtension(dto.getFileName());
        if (dto.getContentType() == null) {
            String mimeType = MimeTypeResolver.getMimeType(ext);
            dto.setContentType(mimeType);
        }

        fileValidator.validateForUpload(dto, user.getUsedQuota());  // 파일 검증
        String objectName = minioFileService.upload(dto, ext);      // 스토리지 업로드
        Metadata metadata = metadataMapper.createMetadata(          // 메타데이터 저장
                user, parentFolder, dto.getFileName(), dto.getFileSize(), objectName, dto.getContentType()
        );
        metadataRepository.save(metadata);
        user.setUsedQuota(user.getUsedQuota() + dto.getFileSize()); // 유저 저장공간 사용량 합산
    }

    @Transactional
    public DownloadFileDto downloadFile(HttpServletResponse res, Long fileId) {
        Metadata metadata = findMetadata(fileId);
        fileValidator.validateFileType(metadata.getType());

        InputStream stream = minioFileService.download(metadata.getFileMetadata().getObjectName());

        return new DownloadFileDto(metadata.getSize(), metadata.getName(), metadata.getFileMetadata().getMimeType(), stream);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
    }

    private Metadata findMetadata(Long metadataId) {
        return metadataRepository.findById(metadataId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FOLDER_NOT_FOUND));
    }
}
