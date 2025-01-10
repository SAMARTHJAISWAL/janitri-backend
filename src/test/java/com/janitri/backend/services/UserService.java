package com.janitri.backend.services;

import com.janitri.backend.models.User;
import com.janitri.backend.repositories.UserRepository;
import com.janitri.backend.dto.UserDTO;
import com.janitri.backend.dto.LoginDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user operations.
 * Handles user registration, authentication, and profile management.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Registers a new user in the system.
     *
     * @param userDTO Data transfer object containing user registration information
     * @return The created user entity
     * @throws RuntimeException if email already exists
     */
    @Transactional
    public User registerUser(UserDTO userDTO) {
        logger.info("Attempting to register new user with email: {}", userDTO.getEmail());
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.error("Registration failed - Email already exists: {}", userDTO.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // In production, password should be encrypted
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());

        User savedUser = userRepository.save(user);
        logger.info("Successfully registered new user with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Authenticates a user based on email and password.
     *
     * @param loginDTO Data transfer object containing login credentials
     * @return The authenticated user
     * @throws RuntimeException if credentials are invalid
     */
    public User login(LoginDTO loginDTO) {
        logger.info("Attempting login for user: {}", loginDTO.getEmail());

        User user = userRepository.findByEmail(loginDTO.getEmail())
                .filter(u -> u.getPassword().equals(loginDTO.getPassword()))
                .orElseThrow(() -> {
                    logger.error("Login failed - Invalid credentials for email: {}", loginDTO.getEmail());
                    return new RuntimeException("Invalid credentials");
                });

        logger.info("Successfully logged in user: {}", user.getEmail());
        return user;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return The user entity
     * @throws ResourceNotFoundException if user not found
     */
    public User getUserById(Long id) {
        logger.debug("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
    }
}