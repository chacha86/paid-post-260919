package com.rest1.standard.ut;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class Ut {
    public static class jwt {
        public static String toString(String secret, long expireSeconds, Map<String, Object> body) {
            ClaimsBuilder claimsBuilder = Jwts.claims();

            for (Map.Entry<String, Object> entry : body.entrySet()) {
                claimsBuilder.add(entry.getKey(), entry.getValue());
            }

            Claims claims = claimsBuilder.build();

            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            String jwt = Jwts.builder()
                    .claims(claims)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact();

            return jwt;
        }

        public static boolean isValid(String jwt, String secretPattern) {

            SecretKey secretKey = Keys.hmacShaKeyFor(secretPattern.getBytes(StandardCharsets.UTF_8));

            try {
                Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwt);

            } catch (Exception e) {
                return false;
            }

            return true;
        }

        public static Map<String, Object> payloadOrNull(String jwt, String secretPattern) {

            SecretKey secretKey = Keys.hmacShaKeyFor(secretPattern.getBytes(StandardCharsets.UTF_8));

            if(isValid(jwt, secretPattern)) {
                return (Map<String, Object>) Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwt)
                        .getPayload();
            }

            return null;
        }
    }
}