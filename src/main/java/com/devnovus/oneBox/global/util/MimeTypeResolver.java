package com.devnovus.oneBox.global.util;

import static com.devnovus.oneBox.global.constant.CommonConstant.DEFAULT_MIME_TYPE;
import static com.devnovus.oneBox.global.constant.CommonConstant.MIME_TYPES;

public class MimeTypeResolver {
    public static String getMimeType(String ext) {
        if (ext == null || ext.isEmpty()) {
            return DEFAULT_MIME_TYPE;
        }
        return MIME_TYPES.getOrDefault(ext.toLowerCase(), DEFAULT_MIME_TYPE);
    }
}
