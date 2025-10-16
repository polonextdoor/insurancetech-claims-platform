package com.insurancetech.service;

import com.insurancetech.dto.AuthResponse;
import com.insurancetech.dto.LoginRequest;
import com.insurancetech.dto.RegisterRequest;
import com.insurancetech.dto.UserResponse;
import com.insurancetech.model.User;
import com.insurancetech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service handling user authentication
 * Manages registration, login, and token generation
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.insurancetech.security.JwtUtil jwtUtil;

    /**
     * Register a new user account
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.CUSTOMER); // Default role
        user.setIsActive(true);

        // Save to database
        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(
        user.getEmail(), 
        user.getId(), 
        user.getRole().name()
    );

        // Return response
        UserResponse userResponse = UserResponse.fromUser(user);
        return new AuthResponse(token, userResponse);
    }

    /**
     * Authenticate user and generate token
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is disabled");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token 
        String token = jwtUtil.generateToken(
        user.getEmail(), 
        user.getId(), 
        user.getRole().name()
    );

        // Return response
        UserResponse userResponse = UserResponse.fromUser(user);
        return new AuthResponse(token, userResponse);
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserResponse.fromUser(user);
    }
}