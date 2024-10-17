package org.zerock.Altari.member.security.auth;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class CustomUserPrincipal implements Principal {

    public final String mid;

    @Override
    public String getName() {
        return mid;
    }
}
