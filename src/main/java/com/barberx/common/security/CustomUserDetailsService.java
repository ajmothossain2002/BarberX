package com.barberx.common.security;

import com.barberx.core.user.entity.User;
import com.barberx.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads user-specific data from the database for Spring Security authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .toList();

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                user.isActive(),
                roles
        );
    }
}
