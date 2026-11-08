package com.janitri.backend.services;

import com.janitri.backend.models.User;
import com.janitri.backend.repositories.UserRepository;
import com.janitri.backend.dto.UserDTO;
import com.janitri.backend.dto.LoginDTO;
import com.janitri.backend.exceptions.DuplicateResourceException;
import com.janitri.backend.exceptions.InvalidCredentialsException;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user (doctor) registration, login and lookup.
 * Passwords are stored as BCrypt hashes and never returned by the API.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerUser(UserDTO userDTO) {
        logger.info("Registering new user with email: {}", userDTO.getEmail());
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());

        return userRepository.save(user);
    }

    public User login(LoginDTO loginDTO) {
        logger.info("Login attempt for user: {}", loginDTO.getEmail());
        return userRepository.findByEmail(loginDTO.getEmail())
                .filter(user -> passwordEncoder.matches(loginDTO.getPassword(), user.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
