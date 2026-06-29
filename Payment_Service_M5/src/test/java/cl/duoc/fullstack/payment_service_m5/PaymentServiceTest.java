package cl.duoc.fullstack.payment_service_m5;

import cl.duoc.fullstack.payment_service_m5.dto.PaymentRequest;
import cl.duoc.fullstack.payment_service_m5.dto.PaymentResponse;
import cl.duoc.fullstack.payment_service_m5.model.Payment;
import cl.duoc.fullstack.payment_service_m5.repository.PaymentRepository;
import cl.duoc.fullstack.payment_service_m5.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
    }

    @Test
    void processPayment_WithValidRequest_ShouldReturnPaymentResponse() {
        // Given
        PaymentRequest request = new PaymentRequest(1L, 99.99);
        Payment saved = new Payment();
        saved.setId(1L);
        saved.setOrderId(1L);
        saved.setAmount(99.99);
        saved.setStatus("SUCCESS");

        when(paymentRepository.save(any())).thenReturn(saved);

        // When
        PaymentResponse response = paymentService.processPayment(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals(99.99, response.amount());
        assertEquals("SUCCESS", response.status());
        verify(paymentRepository).save(any());
    }

    @Test
    void processPayment_ShouldSetStatusToSuccess() {
        // Given
        PaymentRequest request = new PaymentRequest(5L, 250.0);
        Payment saved = new Payment();
        saved.setId(2L);
        saved.setOrderId(5L);
        saved.setAmount(250.0);
        saved.setStatus("SUCCESS");

        when(paymentRepository.save(any())).thenReturn(saved);

        // When
        PaymentResponse response = paymentService.processPayment(request);

        // Then
        assertEquals("SUCCESS", response.status());
    }

    @Test
    void processPayment_WithZeroAmount_ShouldThrowValidation() {
        // Given
        PaymentRequest request = new PaymentRequest(1L, 0.0);

        // When
        Payment saved = new Payment();
        saved.setId(1L);
        saved.setOrderId(1L);
        saved.setAmount(0.0);
        saved.setStatus("SUCCESS");
        when(paymentRepository.save(any())).thenReturn(saved);

        PaymentResponse response = paymentService.processPayment(request);

        // Then
        assertNotNull(response);
        assertEquals(0.0, response.amount());
        verify(paymentRepository).save(any());
    }

    @Test
    void processPayment_ShouldSaveAndReturnNonNullId() {
        // Given
        PaymentRequest request = new PaymentRequest(10L, 150.0);
        Payment saved = new Payment();
        saved.setId(100L);
        saved.setOrderId(10L);
        saved.setAmount(150.0);
        saved.setStatus("SUCCESS");

        when(paymentRepository.save(any())).thenReturn(saved);

        // When
        PaymentResponse response = paymentService.processPayment(request);

        // Then
        assertNotNull(response.id());
        assertEquals(100L, response.id());
    }
}
