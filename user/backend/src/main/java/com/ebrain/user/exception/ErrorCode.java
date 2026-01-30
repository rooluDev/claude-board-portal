package com.ebrain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 인증 관련
    NOT_LOGGED_IN("A005", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAIL("A007", "로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),

    // 회원 관련
    MEMBER_NOT_FOUND("A003", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_ID_EXISTED("A009", "이미 존재하는 아이디입니다.", HttpStatus.CONFLICT),
    JOIN_FAIL("A014", "회원가입에 실패했습니다.", HttpStatus.BAD_REQUEST),

    // 데이터 검증
    ILLEGAL_BOARD_DATA("A013", "잘못된 게시물 데이터입니다.", HttpStatus.BAD_REQUEST),

    // 게시판 관련
    BOARD_NOT_FOUND("A001", "게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_MY_BOARD("A006", "본인의 게시물이 아닙니다.", HttpStatus.FORBIDDEN),

    // 파일 관련
    FILE_NOT_FOUND("A002", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ILLEGAL_FILE_DATA("A008", "잘못된 파일 데이터입니다.", HttpStatus.BAD_REQUEST),

    // 댓글 관련
    COMMENT_NOT_FOUND("A015", "댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_MY_COMMENT("A016", "본인의 댓글이 아닙니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
