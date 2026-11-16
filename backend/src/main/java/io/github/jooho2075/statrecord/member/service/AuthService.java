package io.github.jooho2075.statrecord.member.service;

import io.github.jooho2075.statrecord.global.exception.BusinessException;
import io.github.jooho2075.statrecord.global.exception.ErrorCode;
import io.github.jooho2075.statrecord.global.security.JwtTokenProvider;
import io.github.jooho2075.statrecord.global.security.RefreshTokenService;
import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.member.dto.*;
import io.github.jooho2075.statrecord.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if(memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.create(
                request.email(),
                request.name(),
                passwordEncoder.encode(request.password())
        );
        return new SignupResponse(memberRepository.save(member).getId());
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(member.getId());
    }

    /**재발급: 기존 Refresh Token은 폐기하고 새 토큰 쌍을 발급(Rotation)*/
    public LoginResponse refresh(RefreshTokenRequest request) {
        Long memberId = refreshTokenService.consume(request.refreshToken());
        if(!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return issueTokens(memberId);
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private LoginResponse issueTokens(Long memberId) {
        return LoginResponse.bearer(
                jwtTokenProvider.createAccessToken(memberId),
                refreshTokenService.issue(memberId),
                jwtTokenProvider.getAccessTokenExpiresIn()
        );
    }
}
