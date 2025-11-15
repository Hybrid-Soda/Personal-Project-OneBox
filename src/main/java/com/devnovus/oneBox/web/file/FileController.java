package com.devnovus.oneBox.web.file;

import com.devnovus.oneBox.aop.dto.BaseResponse;
import com.devnovus.oneBox.web.file.dto.UploadFileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            @RequestBody UploadFileRequest request,
            @RequestParam("files") List<MultipartFile> files
    ) {
        fileService.uploadFile(request, files);
        return ResponseEntity.status(201).body(BaseResponse.of("파일업로드완료"));
    }
}
