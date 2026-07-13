package cl.duoc.fullstack.review_service_m7;

import cl.duoc.fullstack.review_service_m7.dto.ReviewRequest;
import cl.duoc.fullstack.review_service_m7.dto.ReviewResponse;
import cl.duoc.fullstack.review_service_m7.model.Review;
import cl.duoc.fullstack.review_service_m7.repository.ReviewRepository;
import cl.duoc.fullstack.review_service_m7.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository);
    }

    @Test
    void createReview_WithValidRequest_ShouldReturnReviewResponse() {
        // Given
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excelente producto, lo recomiendo");
        Review saved = new Review(1L, 1L, 1L, 5, "Excelente producto, lo recomiendo");
        when(reviewRepository.save(any())).thenReturn(saved);

        // When
        ReviewResponse response = reviewService.createReview(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.productId());
        assertEquals(1L, response.userId());
        assertEquals(5, response.rating());
        assertEquals("Excelente producto, lo recomiendo", response.comment());
        verify(reviewRepository).save(any());
    }

    @Test
    void createReview_ShouldSetAllFieldsCorrectly() {
        // Given
        ReviewRequest request = new ReviewRequest(2L, 3L, 3, "Producto regular, cumple con lo esperado");
        Review saved = new Review(2L, 2L, 3L, 3, "Producto regular, cumple con lo esperado");
        when(reviewRepository.save(any())).thenReturn(saved);

        // When
        ReviewResponse response = reviewService.createReview(request);

        // Then
        assertEquals(2L, response.productId());
        assertEquals(3L, response.userId());
        assertEquals(3, response.rating());
        assertEquals("Producto regular, cumple con lo esperado", response.comment());
    }

    @Test
    void createReview_WithInvalidProductId_ShouldThrowException() {
        // Given
        ReviewRequest request = new ReviewRequest(0L, 1L, 4, "Comentario de prueba valido aqui");

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(request));
        assertEquals("El ID del producto debe ser un numero entero positivo mayor a 0", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_WithNullProductId_ShouldThrowException() {
        // Given
        ReviewRequest request = new ReviewRequest(null, 1L, 4, "Comentario de prueba valido aqui");

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(request));
        assertEquals("El ID del producto debe ser un numero entero positivo mayor a 0", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_WithValidRequest_ShouldReturnUpdatedReview() {
        // Given
        Long reviewId = 1L;
        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Producto actualizado con nueva opinion");
        Review existing = new Review(1L, 1L, 1L, 5, "Comentario anterior del producto");
        Review updated = new Review(1L, 1L, 1L, 4, "Producto actualizado con nueva opinion");
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any())).thenReturn(updated);

        // When
        ReviewResponse response = reviewService.updateReview(reviewId, request);

        // Then
        assertNotNull(response);
        assertEquals(4, response.rating());
        assertEquals("Producto actualizado con nueva opinion", response.comment());
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).save(any());
    }

    @Test
    void updateReview_WithNonExistentId_ShouldThrowException() {
        // Given
        Long reviewId = 99L;
        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Intento de actualizar inexistente");
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> reviewService.updateReview(reviewId, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewsByProduct_ShouldReturnListOfReviews() {
        // Given
        Review review1 = new Review(1L, 1L, 1L, 5, "Muy buen producto, supera expectativas");
        Review review2 = new Review(2L, 1L, 2L, 4, "Buen producto, recomendado para todos");
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review1, review2));

        // When
        List<ReviewResponse> responses = reviewService.getReviewsByProduct(1L);

        // Then
        assertEquals(2, responses.size());
        assertEquals(5, responses.get(0).rating());
        assertEquals(4, responses.get(1).rating());
    }
}
