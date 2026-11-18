package io.github.jooho2075.statrecord.team;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jooho2075.statrecord.global.security.JwtTokenProvider;
import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.member.repository.MemberRepository;
import io.github.jooho2075.statrecord.support.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@Transactional
class TeamIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    String managerToken;

    @BeforeEach
    void setUp() {
        Member manager = memberRepository.save(Member.create("manager@test.com", "김감독", "encoded"));
        managerToken = jwtTokenProvider.createAccessToken(manager.getId());
    }

    @Test
    @DisplayName("팀을 만들면 만든 사람이 감독이 되고, 검색 결과에 감독 이름과 인원 수가 나온다")
    void createAndSearchTeam() throws Exception {
        createTeam("썬더스")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("썬더스"));

        mockMvc.perform(get("/api/v1/teams").param("name", "썬더"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].teamName").value("썬더스"))
                .andExpect(jsonPath("$[0].managerNames", contains("김감독")))
                .andExpect(jsonPath("$[0].memberCount").value(1));
    }

    @Test
    @DisplayName("이름이 같은 팀도 만들 수 있고, 감독 이름으로 구분할 수 있다")
    void sameTeamName() throws Exception {
        createTeam("타이거즈");
        Member otherManager = memberRepository.save(Member.create("other@test.com", "이감독", "encoded"));
        String otherToken = jwtTokenProvider.createAccessToken(otherManager.getId());
        mockMvc.perform(post("/api/v1/teams")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"타이거즈\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/teams").param("name", "타이거즈"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("팀 검색은 로그인 없이 할 수 있다")
    void searchWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/v1/teams").param("name", "없는팀"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("팀 생성은 로그인이 필요하다")
    void createTeamWithoutLogin() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"썬더스\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("팀 이름이 비어 있으면 팀을 만들 수 없다")
    void createTeamWithBlankName() throws Exception {
        createTeam("")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    private org.springframework.test.web.servlet.ResultActions createTeam(String name) throws Exception {
        return mockMvc.perform(post("/api/v1/teams")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"%s\"}".formatted(name)));
    }
}