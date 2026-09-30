package com.ticketbooking.service;

import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.dto.RegisterRequest;
import com.ticketbooking.dto.UserResponse;
import com.ticketbooking.model.User;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw conflict("Phone is already registered");
        }
        if (userRepository.existsByCnic(request.getCnic())) {
            throw conflict("CNIC is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .cnic(request.getCnic())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("CUSTOMER")
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> notFound("User is not registered with this phone number"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw unauthorized("Invalid password");
        }
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        String token = jwtService.generateToken(UserPrincipal.from(user), user.getId());
        return UserResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .cnic(user.getCnic())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
