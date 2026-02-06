package com.taskflow.service;

import com.taskflow.dto.AuthenticationRequest;
import com.taskflow.dto.AuthenticationResponse;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.exception.UsernameAlreadyExistsException;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthenticationService {
        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthenticationResponse register(RegisterRequest request) {
                log.debug("Attempting to register user: {}", request.username());
                // Check if username already exists
                if (repository.findByUsername(request.username()).isPresent()) {
                        log.warn("Registration failed: Username '{}' already exists", request.username());
                        throw new UsernameAlreadyExistsException(
                                        "Username already exists. Please choose a different username.");
                }
                var user = User.builder()
                                .firstName(request.firstName())
                                .lastName(request.lastName())
                                .username(request.username())
                                .password(passwordEncoder.encode(request.password()))
                                .build();
                repository.save(user);
                log.info("User registered successfully: {}", request.username());
                var jwtToken = jwtService.generateToken(user);
                return new AuthenticationResponse(jwtToken);
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                log.debug("Attempting to authenticate user: {}", request.username());
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.username(),
                                                request.password()));
                var user = repository.findByUsername(request.username())
                                .orElseThrow();
                log.info("User authenticated successfully: {}", request.username());
                var jwtToken = jwtService.generateToken(user);
                return new AuthenticationResponse(jwtToken);
        }
}
