package io.github.jooho2075.statrecord.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 요청은 @Valid로 검증되고, 실패하면 GlobalExceptionHandlerrk 400으로 응답
public record SignupRequest (
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.") String password,
    @NotBlank @Size(max = 50) String name
) {

}
