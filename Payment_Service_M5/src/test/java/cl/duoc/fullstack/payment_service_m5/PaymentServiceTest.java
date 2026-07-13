package cl.duoc.fullstack.payment_service_m5;

import cl.duoc.fullstack.payment_service_m5.dto.PaymentRequest;
import cl.duoc.fullstack.payment_service_m5.dto.PaymentResponse;
import cl.duoc.fullstack.payment_service_m5.exception.PaymentBusinessException;
import cl.duoc.fullstack.payment_service_m5.model.Payment;
import cl.duoc.fullstack.payment_service_m5.model.PaymentMethod;
import cl.duoc.fullstack.payment_service_m5.model.PaymentStatus;
import cl.duoc.fullstack.payment_service_m5.repository.PaymentRepository;
import cl.duoc.fullstack.payment_service_m5.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

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
        PaymentRequest request = new PaymentRequest(1L, 99.99, PaymentMethod.CREDIT_CARD);
        Payment saved = new Payment();
        saved.setId(1L);
        saved.setOrderId(1L);
        saved.setAmount(99.99);
        saved.setStatus(PaymentStatus.SUCCESS);
        saved.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        saved.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.save(any())).thenReturn(saved);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals(99.99, response.amount());
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(PaymentMethod.CREDIT_CARD, response.paymentMethod());
        verify(paymentRepository).save(any());
    }

    @Test
    void processPayment_ShouldSetStatusToSuccess() {
        PaymentRequest request = new PaymentRequest(5L, 250.0, PaymentMethod.DEBIT_CARD);
        Payment saved = new Payment();
        saved.setId(2L);
        saved.setOrderId(5L);
        saved.setAmount(250.0);
        saved.setStatus(PaymentStatus.SUCCESS);
        saved.setPaymentMethod(PaymentMethod.DEBIT_CARD);
        saved.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.save(any())).thenReturn(saved);

        PaymentResponse response = paymentService.processPayment(request);

        assertEquals(PaymentStatus.SUCCESS, response.status());
    }

    @Test
    void processPayment_WithZeroAmount_ShouldThrowBusinessException() {
        PaymentRequest request = new PaymentRequest(1L, 0.0, PaymentMethod.CASH);

        PaymentBusinessException ex = assertThrows(PaymentBusinessException.class,
                () -> paymentService.processPayment(request));

        assertEquals("INVALID_AMOUNT", ex.getCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_ShouldSaveAndReturnNonNullId() {
        PaymentRequest request = new PaymentRequest(10L, 150.0, PaymentMethod.BANK_TRANSFER);
        Payment saved = new Payment();
        saved.setId(100L);
        saved.setOrderId(10L);
        saved.setAmount(150.0);
        saved.setStatus(PaymentStatus.SUCCESS);
        saved.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        saved.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.save(any())).thenReturn(saved);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response.id());
        assertEquals(100L, response.id());
    }

    @Test
    void processPayment_WithAmountExceedingLimit_ShouldThrowBusinessException() {
        PaymentRequest request = new PaymentRequest(1L, 1_500_000.00, PaymentMethod.CREDIT_CARD);

        PaymentBusinessException ex = assertThrows(PaymentBusinessException.class,
                () -> paymentService.processPayment(request));

        assertEquals("AMOUNT_EXCEEDS_LIMIT", ex.getCode());
        assertTrue(ex.getMessage().contains("límite"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_WithNegativeOrderId_ShouldThrowBusinessException() {
        PaymentRequest request = new PaymentRequest(-1L, 50.0, PaymentMethod.CASH);

        PaymentBusinessException ex = assertThrows(PaymentBusinessException.class,
                () -> paymentService.processPayment(request));

        assertEquals("INVALID_ORDER_ID", ex.getCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getPaymentById_WhenFound_ShouldReturnResponse() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setAmount(99.99);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(PaymentStatus.SUCCESS, response.status());
    }

    @Test
    void getPaymentById_WhenNotFound_ShouldThrowBusinessException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        PaymentBusinessException ex = assertThrows(PaymentBusinessException.class,
                () -> paymentService.getPaymentById(99L));

        assertEquals("PAYMENT_NOT_FOUND", ex.getCode());
    }
}
