package com.govscheme.security;

import com.govscheme.entity.User;
import com.govscheme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrMobileOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(usernameOrMobileOrEmail)
                .orElseGet(() -> userRepository.findByMobile(usernameOrMobileOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email or mobile: " + usernameOrMobileOrEmail)));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
