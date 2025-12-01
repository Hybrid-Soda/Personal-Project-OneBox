package com.devnovus.oneBox.domain.file.controller;

import com.devnovus.oneBox.domain.file.dto.PreSignedUrlRequest;
import com.devnovus.oneBox.domain.file.dto.PreSignedUrlResponse;
import com.devnovus.oneBox.domain.file.service.FileDataServiceV3;
import com.devnovus.oneBox.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files/v3")
@RequiredArgsConstructor
public class FileDataControllerV3 {
    private final FileDataServiceV3 fileService;

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
    public ResponseEntity<BaseResponse<String>> completePreSignedUpload(
            @PathVariable Long fileId,
            @RequestParam String eTag
    ) {
        fileService.completePreSignedUpload(fileId, eTag);
        return ResponseEntity.ok(BaseResponse.of("업로드 완료"));
    }
}
