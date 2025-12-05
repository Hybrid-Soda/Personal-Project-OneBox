package com.devnovus.oneBox.domain.file.controller;

import com.devnovus.oneBox.domain.file.dto.FileDownloadDto;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlRequest;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.dto.FileUploadDto;
import com.devnovus.oneBox.domain.file.service.FileDataService;
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
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileDataController {
    private final FileDataService fileService;

    /** 파일업로드 - multipart 방식 */
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest req,
            @RequestPart("file") MultipartFile file
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            long userId = Long.parseLong(requiredHeader(req, "User-Id"));
            long parentFolderId = Long.parseLong(requiredHeader(req, "Parent-Folder-Id"));

            FileUploadDto dto = new FileUploadDto(
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

            FileUploadDto dto = new FileUploadDto(
                    userId, parentFolderId, req.getContentLengthLong(),
                    originalFilename, null, inputStream
            );

            fileService.uploadFile(dto);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    /** pre-signed URL 생성 */
    @PostMapping("/pre-signed")
    public ResponseEntity<BaseResponse<PreSignedUrlResponse>> createPreSignedUrl(
            @RequestBody PreSignedUrlRequest request
    ) {
        PreSignedUrlResponse response = fileService.createPreSignedUrl(request);
        return ResponseEntity.status(201).body(BaseResponse.of(response));
    }

    /** pre-signed 업로드 완료 처리 */
    @PostMapping("/pre-signed/{fileId}/complete")
    public ResponseEntity<BaseResponse<String>> completeUpload(
            @PathVariable Long fileId,
            @RequestParam String eTag
    ) {
        fileService.completeUpload(fileId, eTag);
        return ResponseEntity.ok(BaseResponse.of("업로드 완료"));
    }

    /** 파일다운로드 */
    @GetMapping("/{fileId}/download")
    public void downloadFile(
            HttpServletResponse res,
            @PathVariable Long fileId
    ) throws IOException {
        FileDownloadDto dto = fileService.downloadFile(fileId);

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
