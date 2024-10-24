package org.zerock.Altari.security.auth;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class CustomUserPrincipal implements Principal {

    public final String username; //

    @Override
    public String getName() {
        return username; //
    }
}
