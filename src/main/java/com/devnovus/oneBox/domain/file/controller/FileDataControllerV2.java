package com.devnovus.oneBox.domain.file.controller;

import com.devnovus.oneBox.domain.file.dto.DownloadFileDto;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.domain.file.service.FileDataServiceV2;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files/v2")
@RequiredArgsConstructor
public class FileDataControllerV2 {
    private final FileDataServiceV2 fileService;

    /** 파일업로드 - multipart 방식 */
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest req,
            @RequestPart("file") MultipartFile file
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            long userId = Long.parseLong(requiredHeader(req, "User-Id"));
            long parentFolderId = Long.parseLong(requiredHeader(req, "Parent-Folder-Id"));

            UploadFileDto dto = new UploadFileDto(
                    userId, parentFolderId, file.getSize(),
                    file.getOriginalFilename(), file.getContentType(), inputStream
            );

            fileService.uploadFile(dto);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    /** 파일업로드 - binary stream 방식 */
    @PostMapping("/upload-stream")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest req
    ) {
        try (InputStream inputStream = req.getInputStream()) {
            long userId = Long.parseLong(requiredHeader(req, "User-Id"));
            long parentFolderId = Long.parseLong(requiredHeader(req, "Parent-Folder-Id"));
            String originalFilename = requiredHeader(req, "Original-Filename");

            UploadFileDto dto = new UploadFileDto(
                    userId, parentFolderId, req.getContentLengthLong(),
                    originalFilename, null, inputStream
            );

            fileService.uploadFile(dto);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    /** 파일다운로드 */
    @GetMapping("/{fileId}/download")
    public void downloadFile(
            HttpServletResponse res,
            @PathVariable Long fileId
    ) throws IOException {
        DownloadFileDto dto = fileService.downloadFile(fileId);

        res.setContentType(dto.getMimeType());
        res.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(dto.getFileSize()));
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8) + "\"");
        StreamUtils.copy(dto.getInputStream(), res.getOutputStream());
    }

    private String requiredHeader(HttpServletRequest req, String name) {
        String value = req.getHeader(name);

        if (value == null || value.isBlank()) {
            throw new ApplicationException(ApplicationError.MISSING_REQUIRED_HEADER);
        }

        return value;
    }
}
