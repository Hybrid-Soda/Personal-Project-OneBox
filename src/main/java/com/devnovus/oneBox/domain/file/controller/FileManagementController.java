package com.devnovus.oneBox.domain.file.controller;

import com.devnovus.oneBox.domain.file.dto.MoveFileRequest;
import com.devnovus.oneBox.domain.file.dto.UpdateFileNameRequest;
import com.devnovus.oneBox.domain.file.service.FileManagementService;
import com.devnovus.oneBox.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileManagementController {
    private final FileManagementService fileService;

    /** 파일이동 */
    @PatchMapping("/{fileId}/move")
    public ResponseEntity<BaseResponse<String>> moveFile(
            @PathVariable Long fileId,
            @RequestBody MoveFileRequest request
    ) {
        fileService.moveFile(fileId, request);
        return ResponseEntity.ok().body(BaseResponse.of("파일 이동 완료"));
    }

    /** 파일이름수정 */
    @PatchMapping("/{fileId}/name")
    public ResponseEntity<BaseResponse<String>> updateFileName(
            @PathVariable Long fileId,
            @RequestBody UpdateFileNameRequest request
    ) {
        fileService.updateFileName(fileId, request);
        return ResponseEntity.ok().body(BaseResponse.of("파일 이름 수정 완료"));
    }

    /** 파일삭제 */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> removeFile(
            @PathVariable Long fileId
    ) {
        fileService.removeFile(fileId);
        return ResponseEntity.noContent().build();
    }
}
