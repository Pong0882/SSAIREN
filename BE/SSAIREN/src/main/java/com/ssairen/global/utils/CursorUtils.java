package com.ssairen.global.utils;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CursorUtils {

    private CursorUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * ID를 Base64로 인코딩하여 커서 문자열 생성
     *
     * @param id 인코딩할 ID
     * @return Base64로 인코딩된 커서 문자열
     */
    public static String encodeCursor(Long id) {
        if (id == null) {
            return null;
        }
        String idString = String.valueOf(id);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(idString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64로 인코딩된 커서 문자열을 디코딩하여 ID 반환
     *
     * @param cursor Base64로 인코딩된 커서 문자열
     * @return 디코딩된 ID
     * @throws CustomException 유효하지 않은 커서 형식인 경우
     */
    public static Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            return Long.parseLong(decodedString);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }
}
