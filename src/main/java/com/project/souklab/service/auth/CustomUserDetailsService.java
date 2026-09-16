package com.project.souklab.service.auth;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.core.userdetails.User.builder;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(emailOrUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailOrUsername));

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (AuthorizationPermission permission : user.getPermissions()) {
            if (permission.isEnabled()) {
                authorities.add(new SimpleGrantedAuthority(permission.getPermissionKey()));
            }
        }

        boolean isAccountLocked = user.isSuspensionActive(LocalDateTime.now(clock));
        boolean isAccountDisabled = user.getStatus() != AccountStatus.ACTIVE;

        String password = user.getPassword() != null ? user.getPassword() : "";

        return builder()
                .username(user.getEmail())
                .password(password)
                .disabled(isAccountDisabled)
                .accountLocked(isAccountLocked)
                .authorities(authorities)
                .build();
    }
}
