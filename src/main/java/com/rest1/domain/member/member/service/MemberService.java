package com.rest1.domain.member.member.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder;

    public long count() {
        return memberRepository.count();
    }

    public Member join(String username, String password, String nickname) {

        memberRepository.findByUsername(username)
                .ifPresent(m -> {
                    throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                });

        Member member = new Member(username, passwordEncoder.encode(password), nickname);
        return memberRepository.save(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }

    public Map<String, Object> payloadOrNull(String accessToken) {
        return authTokenService.payloadOrNull(accessToken);
    }

    public Optional<Member> findById(long id) {
        return memberRepository.findById(id);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public void checkPassword(String inputPassword, String rawPassword) {
        if(!passwordEncoder.matches(inputPassword, rawPassword)) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }
    }
}
