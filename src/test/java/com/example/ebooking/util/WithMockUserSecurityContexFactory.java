package com.example.ebooking.util;

import com.example.ebooking.model.Role;
import com.example.ebooking.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

final class WithMockUserSecurityContexFactory implements
        WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser withUser) {
        String username = StringUtils.hasLength(withUser.username())
                ? withUser.username() : withUser.value();

        Assert.notNull(username, () -> withUser
                + " cannot have null username on both username and value properties");

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (String authority : withUser.authorities()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }

        if (grantedAuthorities.isEmpty()) {
            for (String role : withUser.roles()) {
                Assert.isTrue(!role.startsWith("ROLE_"), () -> "roles cannot "
                        + "start with ROLE_ Got " + role);
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        } else if (!(withUser.roles().length == 1 && "USER".equals(withUser
                .roles()[0]))) {
            throw new IllegalStateException("You cannot define roles attribute "
                    + Arrays.asList(withUser.roles())
                    + " with authorities attribute "
                    + Arrays.asList(withUser.authorities()));
        }

        User principal = new User();
        principal.setEmail(withUser.email());
        principal.setFirstName(withUser.name());
        principal.setId(2L);

        Set<Role> roles = grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> {
                    Role role = new Role();
                    role.setRole(Role.RoleName.valueOf(authority));
                    return role;
                })
                .collect(Collectors.toSet());

        principal.setRoles(roles);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, grantedAuthorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
