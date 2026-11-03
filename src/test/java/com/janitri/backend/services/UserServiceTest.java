package com.janitri.backend.services;

import com.janitri.backend.dto.LoginDTO;
import com.janitri.backend.dto.UserDTO;
import com.janitri.backend.exceptions.DuplicateResourceException;
import com.janitri.backend.exceptions.InvalidCredentialsException;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import com.janitri.backend.models.User;
import com.janitri.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDTO = new UserDTO();
        userDTO.setName("Dr. Smith");
        userDTO.setEmail("dr.smith@example.com");
        userDTO.setPassword("secret123");
        userDTO.setRole("DOCTOR");
    }

    private User storedUser(String rawPassword) {
        User user = new User();
        user.setId(1L);
        user.setEmail(userDTO.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        return user;
    }

    @Test
    void whenRegisterUser_thenReturnSavedUserWithHashedPassword() {
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(userDTO);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        String stored = captor.getValue().getPassword();
        assertNotEquals("secret123", stored, "password must not be stored in plain text");
        assertTrue(new BCryptPasswordEncoder().matches("secret123", stored));
        assertEquals("dr.smith@example.com", saved.getEmail());
    }

    @Test
    void whenRegisterUserWithExistingEmail_thenThrowException() {
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnUser() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(storedUser("secret123")));
        LoginDTO login = new LoginDTO();
        login.setEmail(userDTO.getEmail());
        login.setPassword("secret123");

        assertEquals(1L, userService.login(login).getId());
    }

    @Test
    void whenLoginWithInvalidCredentials_thenThrowException() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(storedUser("secret123")));
        LoginDTO login = new LoginDTO();
        login.setEmail(userDTO.getEmail());
        login.setPassword("wrong");

        assertThrows(InvalidCredentialsException.class, () -> userService.login(login));
    }

    @Test
    void whenLoginWithUnknownEmail_thenThrowException() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        LoginDTO login = new LoginDTO();
        login.setEmail("nobody@example.com");
        login.setPassword("secret123");

        assertThrows(InvalidCredentialsException.class, () -> userService.login(login));
    }

    @Test
    void whenGetUnknownUser_thenThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }
}
