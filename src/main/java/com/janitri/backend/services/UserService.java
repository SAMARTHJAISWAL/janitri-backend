package com.janitri.backend.services;

import com.janitri.backend.models.User;
import com.janitri.backend.repositories.UserRepository;
import com.janitri.backend.dto.UserDTO;
import com.janitri.backend.dto.LoginDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User registerUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // In production, password should be encrypted
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());

        return userRepository.save(user);
    }

    public User login(LoginDTO loginDTO) {
        return userRepository.findByEmail(loginDTO.getEmail())
                .filter(user -> user.getPassword().equals(loginDTO.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}