package io.github.jooho2075.statrecord.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }

        @GetMapping("/test/illegal-state")
        void illegalState() {
            throw new IllegalStateException("이미 확정된 기록입니다.");
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new RuntimeException("DB 접속 정보 같은 내부 메시지");
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }
    }

    record TestRequest(@NotBlank(message = "필수 값입니다.") String name) {
    }

    @Test
    @DisplayName("BusinessException은 ErrorCode의 상태 코드와 메시지로 응답한다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("팀을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("엔티티의 상태 규칙 위반은 409로 응답한다")
    void illegalState() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("이미 확정된 기록입니다."));
    }

    @Test
    @DisplayName("요청 값 검증 실패는 400과 필드 메시지로 응답한다")
    void validation() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("name: 필수 값입니다."));
    }

    @Test
    @DisplayName("예상하지 못한 예외는 내부 메시지를 숨기고 500으로 응답한다")
    void unexpected() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("없는 주소로 요청하면 500이 아니라 404로 응답한다")
    void notFound() throws Exception {
        mockMvc.perform(get("/test/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}