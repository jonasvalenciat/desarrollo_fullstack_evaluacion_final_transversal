package cl.duoc.fullstack.cart_service_m3;

import cl.duoc.fullstack.cart_service_m3.client.NotificationClient;
import cl.duoc.fullstack.cart_service_m3.dto.CartItemCommand;
import cl.duoc.fullstack.cart_service_m3.dto.CartItemResult;
import cl.duoc.fullstack.cart_service_m3.exception.BadRequestException;
import cl.duoc.fullstack.cart_service_m3.model.User;
import cl.duoc.fullstack.cart_service_m3.repository.CartHistoryRepository;
import cl.duoc.fullstack.cart_service_m3.repository.CartRepository;
import cl.duoc.fullstack.cart_service_m3.repository.UserRepository;
import cl.duoc.fullstack.cart_service_m3.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartHistoryRepository historyRepository;
    @Mock
    private NotificationClient notificationClient;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, userRepository, historyRepository, notificationClient);
    }

    @Test
    void addToCart_WithValidCommand_ShouldReturnCartItemResult() {
        // Given
        User user = new User(1L, "Ana Garcia", "ana.garcia@empresa.com", null, User.Role.USER, true);
        CartItemCommand command = new CartItemCommand("Laptop", 1500.0, 1, "ana.garcia@empresa.com", null);

        when(cartRepository.existsByProductNameIgnoreCase("Laptop")).thenReturn(false);
        when(userRepository.findByEmail("ana.garcia@empresa.com")).thenReturn(Optional.of(user));
        when(cartRepository.save(any())).thenAnswer(invocation -> {
            var item = invocation.<cl.duoc.fullstack.cart_service_m3.model.CartItem>getArgument(0);
            item.setId(1L);
            return item;
        });

        // When
        CartItemResult result = cartService.addToCart(command);

        // Then
        assertNotNull(result);
        assertEquals("Laptop", result.productName());
        assertEquals(1500.0, result.price());
        assertEquals(1, result.quantity());
        assertEquals("PENDING", result.status());
        verify(cartRepository).save(any());
        verify(historyRepository).save(any());
    }

    @Test
    void addToCart_WithDuplicateProduct_ShouldThrowException() {
        // Given
        CartItemCommand command = new CartItemCommand("Laptop", 1500.0, 1, "ana.garcia@empresa.com", null);
        when(cartRepository.existsByProductNameIgnoreCase("Laptop")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(command));
        assertTrue(exception.getMessage().contains("Ya existe un producto con el nombre"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addToCart_WithNonExistentUser_ShouldThrowBadRequestException() {
        // Given
        CartItemCommand command = new CartItemCommand("Laptop", 1500.0, 1, "noexiste@email.com", null);
        when(cartRepository.existsByProductNameIgnoreCase("Laptop")).thenReturn(false);
        when(userRepository.findByEmail("noexiste@email.com")).thenReturn(Optional.empty());

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> cartService.addToCart(command));
        assertTrue(exception.getMessage().contains("No existe un usuario con el email"));
    }

    @Test
    void addToCart_WithCouponCodeEqualToUserEmail_ShouldThrowException() {
        // Given
        User user = new User(1L, "Ana Garcia", "ana.garcia@empresa.com", null, User.Role.USER, true);
        CartItemCommand command = new CartItemCommand("Laptop", 1500.0, 1, "ana.garcia@empresa.com", "ana.garcia@empresa.com");
        when(cartRepository.existsByProductNameIgnoreCase("Laptop")).thenReturn(false);
        when(userRepository.findByEmail("ana.garcia@empresa.com")).thenReturn(Optional.of(user));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(command));
        assertTrue(exception.getMessage().contains("El c\u00f3digo de cup\u00f3n no puede ser igual al email del usuario"));
    }
}
