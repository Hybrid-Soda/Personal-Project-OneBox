package com.devnovus.oneBox.domain.file.controller;

import com.devnovus.oneBox.domain.file.dto.FileDownloadResponse;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.dto.FileUploadRequest;
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
    @PutMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest httpServletRequest,
            @RequestPart("file") MultipartFile file
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            long parentFolderId = Long.parseLong(requiredHeader(httpServletRequest, "Parent-Folder-Id"));

            FileUploadRequest req = new FileUploadRequest(
                    parentFolderId, file.getSize(), file.getOriginalFilename(), file.getContentType(), inputStream
            );

            fileService.uploadFile(req);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    /** 파일업로드 - binary stream 방식 */
    @PutMapping("/upload-stream")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest httpServletRequest
    ) {
        try (InputStream inputStream = httpServletRequest.getInputStream()) {
            long parentFolderId = Long.parseLong(requiredHeader(httpServletRequest, "Parent-Folder-Id"));
            String originalFilename = requiredHeader(httpServletRequest, "Original-Filename");

            FileUploadRequest req = new FileUploadRequest(
                    parentFolderId, httpServletRequest.getContentLengthLong(), originalFilename, null, inputStream
            );

            fileService.uploadFile(req);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    /** pre-signed URL 생성 */
    @PostMapping("/pre-signed")
    public ResponseEntity<BaseResponse<PreSignedUrlResponse>> createPreSignedUrl(
            @RequestBody FileUploadRequest req
    ) {
        PreSignedUrlResponse response = fileService.createPreSignedUrl(req);
        return ResponseEntity.status(201).body(BaseResponse.of(response));
    }

    /** pre-signed 업로드 완료 처리 */
    @PatchMapping("/pre-signed/{fileId}/complete")
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
            HttpServletResponse httpServletResponse,
            @PathVariable Long fileId
    ) throws IOException {
        FileDownloadResponse res = fileService.downloadFile(fileId);

        httpServletResponse.setContentType(res.getMimeType());
        httpServletResponse.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(res.getFileSize()));
        httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + URLEncoder.encode(res.getFileName(), StandardCharsets.UTF_8) + "\"");
        StreamUtils.copy(res.getInputStream(), httpServletResponse.getOutputStream());
    }

    private String requiredHeader(HttpServletRequest req, String name) {
        String value = req.getHeader(name);

        if (value == null || value.isBlank()) {
            throw new ApplicationException(ApplicationError.MISSING_REQUIRED_HEADER);
        }

        return value;
    }
}
