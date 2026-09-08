package com.rest1.domain.member.member.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.standard.ut.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secretPattern}")
    private String secretPattern;
    @Value("${custom.jwt.expireSeconds}")
    private long expireSeconds;

    String genAccessToken(Member member) {

        return Ut.jwt.toString(
                secretPattern,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername(), "nickname", member.getNickname())
        );
    }

    Map<String, Object> payloadOrNull(String jwt) {
        Map<String, Object> payload = Ut.jwt.payloadOrNull(jwt, secretPattern);

        if(payload == null) {
            return null;
        }

        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();

        String username = (String)payload.get("username");
        String nickname = (String)payload.get("nickname");

       return Map.of("id", id, "username", username, "nickname", nickname);
    }
}
