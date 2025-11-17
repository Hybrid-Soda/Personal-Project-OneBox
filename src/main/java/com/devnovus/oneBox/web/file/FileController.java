package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("userId") Long userId,
            @RequestParam("parentFolderId") Long parentFolderId
    ) {
        fileService.uploadFile(userId, parentFolderId, files);
        return ResponseEntity.status(201).body(BaseResponse.of("파일업로드완료"));
    }
}
