package com.ebrain.user.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    /**
     * SHA2-256 해싱 (MySQL SHA2(password, 256)와 호환)
     */
    public static String hashWithSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 해싱 실패", e);
        }
    }

    /**
     * 비밀번호 유효성 검증
     */
    public static void validatePassword(String password, String memberId) {
        // 동일 문자 3개 연속 금지
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                password.charAt(i) == password.charAt(i + 2)) {
                throw new IllegalArgumentException("동일 문자 3개 연속 사용 불가");
            }
        }

        // 아이디와 동일 금지
        if (password.equals(memberId)) {
            throw new IllegalArgumentException("비밀번호는 아이디와 같을 수 없습니다.");
        }
    }
}
