package com.abi.service.impl;

import com.abi.constant.SecurityConstants;
import com.abi.entity.User;
import com.abi.exception.DuplicateResourceException;
import com.abi.exception.InvalidCredentialsException;
import com.abi.repository.UserRepository;
import com.abi.request.LoginRequest;
import com.abi.request.RegisterRequest;
import com.abi.response.AuthResponse;
import com.abi.security.JwtTokenProvider;
import com.abi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	@Autowired
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(final LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (final BadCredentialsException | LockedException | DisabledException ex) {
            log.warn("Authentication failed for username [{}]: {}", loginRequest.getUsername(), ex.getMessage());
            throw new InvalidCredentialsException();
        }

        final User user = userRepository.findByUsernameAndIsDeletedFalse(loginRequest.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        log.info("User [{}] authenticated successfully", user.getUsername());

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse register(final RegisterRequest registerRequest) {
        if (userRepository.existsByUsernameAndIsDeletedFalse(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmailAndIsDeletedFalse(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        final User user = User.builder()
                .username(registerRequest.getUsername())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(registerRequest.getRole())
                .build();

        userRepository.save(user);

        log.info("New user registered: [{}]", user.getUsername());

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(final User user) {
        final String accessToken = jwtTokenProvider.generateAccessToken(user);
        final String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.TOKEN_TYPE)
                .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .build();
    }
}
