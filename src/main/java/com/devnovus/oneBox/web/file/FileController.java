package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            @RequestParam("parentFolderId") Long parentFolderId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        fileService.uploadFile(parentFolderId, files);
        return ResponseEntity.status(201).body(BaseResponse.of("파일업로드완료"));
    }
}
