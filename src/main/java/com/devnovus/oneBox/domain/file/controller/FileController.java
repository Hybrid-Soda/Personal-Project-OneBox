package com.devnovus.oneBox.domain.file.controller;

import com.devnovus.oneBox.global.response.BaseResponse;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.file.service.FileService;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest req,
            @RequestPart("file") MultipartFile file
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            // 헤더 추출
            long userId = Long.parseLong(requiredHeader(req, "User-Id"));
            long parentFolderId = Long.parseLong(requiredHeader(req, "Parent-Folder-Id"));

            // Dto 생성
            UploadFileDto dto = new UploadFileDto(
                    userId, parentFolderId, file.getSize(),
                    file.getOriginalFilename(), file.getContentType(), inputStream
            );

            // 업로드 수행
            fileService.uploadFile(dto);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    @PostMapping("/upload-stream")
    public ResponseEntity<BaseResponse<String>> uploadFile(HttpServletRequest req) {
        try (InputStream inputStream = req.getInputStream()) {
            // 헤더 추출
            long userId = Long.parseLong(requiredHeader(req, "User-Id"));
            long parentFolderId = Long.parseLong(requiredHeader(req, "Parent-Folder-Id"));
            String originalFilename = requiredHeader(req, "Original-Filename");

            // Dto 생성
            UploadFileDto dto = new UploadFileDto(
                    userId, parentFolderId, req.getContentLengthLong(),
                    originalFilename, null, inputStream
            );

            // 업로드 수행
            fileService.uploadFile(dto);
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    @GetMapping("/pre-signed")
    public ResponseEntity<BaseResponse<String>> getPreSignedUrl(HttpServletRequest req) {
        // 헤더 추출
        long userId = Long.parseLong(requiredHeader(req, "User-Id"));
        long parentFolderId = Long.parseLong(requiredHeader(req, "Parent-Folder-Id"));
        long fileSize = Long.parseLong(requiredHeader(req, "File-Size"));
        String originalFilename = requiredHeader(req, "Original-Filename");

        // Dto 생성
        UploadFileDto dto = new UploadFileDto(userId, parentFolderId, fileSize, originalFilename, null, null);

        // 업로드 수행
        String url = fileService.getPreSignedUrl(dto);
        return ResponseEntity.status(201).body(BaseResponse.of(url));
    }

    private String requiredHeader(HttpServletRequest req, String name) {
        String value = req.getHeader(name);

        if (value == null || value.isBlank()) {
            throw new ApplicationException(ApplicationError.MISSING_REQUIRED_HEADER);
        }

        return value;
    }
}
