package cl.duoc.fullstack.product_service.service;

import cl.duoc.fullstack.product_service.model.Product;
import cl.duoc.fullstack.product_service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts_whenProductsExist_shouldReturnList() {
        // Given
        List<Product> products = List.of(
                new Product(1L, "Teclado", 25000.0, 10),
                new Product(2L, "Mouse", 15000.0, 20)
        );
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Teclado", result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void createProduct_whenValidProduct_shouldSaveAndReturn() {
        // Given
        Product product = new Product(null, "Teclado", 25000.0, 10);
        Product savedProduct = new Product(1L, "Teclado", 25000.0, 10);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        Product result = productService.createProduct(product);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Teclado", result.getName());
        assertEquals(25000.0, result.getPrice());
        assertEquals(10, result.getStock());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() {
        // Given
        Product product = new Product(1L, "Teclado", 25000.0, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        Product result = productService.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Teclado", result.getName());
        assertEquals(25000.0, result.getPrice());
        assertEquals(10, result.getStock());
    }

    @Test
    void getProductById_whenProductNotFound_shouldThrowEntityNotFoundException() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> productService.getProductById(99L));
        assertEquals("Producto no encontrado con id: 99", exception.getMessage());
    }
}
