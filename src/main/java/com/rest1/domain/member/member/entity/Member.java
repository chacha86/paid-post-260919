package com.rest1.domain.member.member.entity;

import com.rest1.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
public class Member extends BaseEntity {

    @Column(unique = true)
    private String username;
    private String password;
    private String nickname;
    @Column(unique = true)
    private String apiKey;

    public Member(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.apiKey = UUID.randomUUID().toString();
    }

    public Member(Long id, String username, String nickname) {
        this.setId(id);
        this.username = username;
        this.nickname = nickname;
    }

    public String getName() {
        return nickname;
    }

    public void updateApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isAdmin() {
        return "admin".equals(this.username);
    }

    // JWT의 roles 클레임에 실을 값. 시큐리티 권한명(ROLE_ADMIN)이 아니라 우리 도메인 단어(ADMIN)를 쓴다.
    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();

        if(isAdmin()) {
            roles.add("ADMIN");
        }

        return roles;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if(isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return authorities;
    }
}

