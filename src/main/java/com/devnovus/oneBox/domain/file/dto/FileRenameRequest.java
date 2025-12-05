package com.devnovus.oneBox.domain.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.devnovus.oneBox.global.constant.CommonConstant.MAX_FILE_NAME_LENGTH;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileRenameRequest {
    @NotBlank(message = "파일 이름은 비어 있을 수 없습니다.")
    @Size(max = MAX_FILE_NAME_LENGTH, message = "파일 이름은 최대 " + MAX_FILE_NAME_LENGTH + "자까지 허용됩니다.")
    @Pattern(regexp = "^[^\\\\/:*?\"<>|]+$", message = "파일 이름에 허용되지 않는 특수문자가 포함되어 있습니다.")
    private String fileName;     // 파일이름
}
