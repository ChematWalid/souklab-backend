package com.project.souklab.service.user;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.User;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Resolves the authenticated application user for service-layer operations. */
@Service
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User requireCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null || username.isBlank()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByEmail(username.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
