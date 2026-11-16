package io.github.jooho2075.statrecord.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.member.repository.MemberRepository;
import io.github.jooho2075.statrecord.support.TestContainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@Transactional
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final String EMAIL = "player@test.com";
    private static final String PASSWORD = "password123!";

    // ===== 회원가입·로그인 =====

    @Test
    @DisplayName("회원가입 후 로그인하면 받은 토큰으로 내 정보를 조회할 수 있다")
    void signupLoginAndGetMe() throws Exception {
        signup(EMAIL, PASSWORD);
        String token = loginAndGet(EMAIL, PASSWORD, "$.accessToken");

        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("선수"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호는 암호화되어 저장된다")
    void passwordIsEncoded() throws Exception {
        signup(EMAIL, PASSWORD);

        Member saved = memberRepository.findByEmail(EMAIL).orElseThrow();

        assertThat(saved.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("토큰 없이 내 정보를 조회하면 401로 응답한다")
    void getMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("변조된 토큰으로 요청하면 401로 응답한다")
    void getMeWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인할 수 없다")
    void loginWithWrongPassword() throws Exception {
        signup(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(EMAIL, "wrongpassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("이미 가입된 이메일로는 회원가입할 수 없다")
    void signupWithDuplicateEmail() throws Exception {
        signup(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(EMAIL, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("이메일 형식이 아니면 회원가입할 수 없다")
    void signupWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("not-an-email", PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ===== Refresh Token =====

    @Test
    @DisplayName("Refresh Token으로 새 토큰을 발급받으면 새 Access Token으로 API를 호출할 수 있다")
    void refreshToken() throws Exception {
        signup(EMAIL, PASSWORD);
        String refreshToken = loginAndGet(EMAIL, PASSWORD, "$.refreshToken");

        String response = refresh(refreshToken)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newAccessToken = JsonPath.read(response, "$.accessToken");
        String newRefreshToken = JsonPath.read(response, "$.refreshToken");

        assertThat(newRefreshToken).isNotEqualTo(refreshToken);
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("재발급에 한 번 사용한 Refresh Token은 다시 사용할 수 없다")
    void refreshTokenRotation() throws Exception {
        signup(EMAIL, PASSWORD);
        String refreshToken = loginAndGet(EMAIL, PASSWORD, "$.refreshToken");
        refresh(refreshToken).andExpect(status().isOk());

        refresh(refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("로그아웃하면 Refresh Token으로 재발급할 수 없다")
    void logout() throws Exception {
        signup(EMAIL, PASSWORD);
        String refreshToken = loginAndGet(EMAIL, PASSWORD, "$.refreshToken");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isNoContent());

        refresh(refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("발급되지 않은 Refresh Token으로는 재발급할 수 없다")
    void refreshWithUnknownToken() throws Exception {
        refresh("unknown-refresh-token")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    // ===== 도우미 메서드 =====

    private void signup(String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, password)))
                .andExpect(status().isCreated());
    }

    /** 로그인 후 응답에서 원하는 값(accessToken, refreshToken)을 꺼냄 */
    private String loginAndGet(String email, String password, String jsonPath) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, jsonPath);
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)));
    }

    private String signupBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s", "name": "선수"}
                """.formatted(email, password);
    }

    private String loginBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    private String refreshBody(String refreshToken) {
        return """
                {"refreshToken": "%s"}
                """.formatted(refreshToken);
    }
}