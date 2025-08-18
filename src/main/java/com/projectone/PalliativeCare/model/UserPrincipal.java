package com.projectone.PalliativeCare.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Use email as the username for Spring Security
    }


    @Override
    public boolean isAccountNonExpired() {
        return true; // customize later if you want account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // customize later if you want lock/unlock feature
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // customize later if you want credential expiration
    }

    @Override
    public boolean isEnabled() {
        return true; // customize later for active/inactive accounts
    }

    // Extra helper: get the actual User object if needed
//    public User getUser() {
//        return user;
//    }
}
