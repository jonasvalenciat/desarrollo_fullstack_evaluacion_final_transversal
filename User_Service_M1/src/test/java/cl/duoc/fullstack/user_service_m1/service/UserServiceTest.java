package cl.duoc.fullstack.user_service_m1.service;

import cl.duoc.fullstack.user_service_m1.model.User;
import cl.duoc.fullstack.user_service_m1.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenValidUser_shouldHashPasswordAndSave() {
        // Given
        User user = new User(null, "Juan Pérez", "juan@example.com", "plain123");
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("$2a$10$encodedPassword");
        User savedUser = new User(1L, "Juan Pérez", "juan@example.com", "$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(user);

        // Then
        assertNotNull(result);
        assertEquals("$2a$10$encodedPassword", result.getPassword());
        assertEquals(1L, result.getId());
        verify(passwordEncoder).encode("plain123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenDuplicateEmail_shouldThrowException() {
        // Given
        User user = new User(null, "Juan Pérez", "duplicate@example.com", "plain123");
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));
        assertEquals("Email already registered: duplicate@example.com", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        // Given
        User user = new User(1L, "Juan Pérez", "juan@example.com", "$2a$10$encodedPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Juan Pérez", result.getName());
        assertEquals("juan@example.com", result.getEmail());
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowEntityNotFoundException() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.getUserById(99L));
        assertEquals("User not found with id: 99", exception.getMessage());
    }
}
