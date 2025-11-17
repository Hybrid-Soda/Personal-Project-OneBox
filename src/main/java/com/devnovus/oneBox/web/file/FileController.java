package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.dto.BaseResponse;
import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.web.file.dto.UploadFileDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            HttpServletRequest req,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            long userId = Long.parseLong(req.getHeader("User-Id"));
            long parentFolderId = Long.parseLong(req.getHeader("Parent-Folder-Id"));

            fileService.uploadFile(new UploadFileDto(
                    userId, parentFolderId, file.getSize(),
                    file.getOriginalFilename(), file.getContentType(), inputStream
            ));
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }

    @PostMapping("/upload-stream")
    public ResponseEntity<BaseResponse<String>> uploadFile(HttpServletRequest req) {
        try (InputStream inputStream = req.getInputStream()) {
            long userId = Long.parseLong(req.getHeader("User-Id"));
            long parentFolderId = Long.parseLong(req.getHeader("Parent-Folder-Id"));
            String originalFilename = req.getHeader("Original-Filename");


            fileService.uploadFile(new UploadFileDto(
                    userId, parentFolderId, req.getContentLengthLong(),
                    originalFilename, null, inputStream
            ));
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 완료"));
        } catch (IOException e) {
            return ResponseEntity.status(201).body(BaseResponse.of("업로드 실패: " + e));
        }
    }
}
