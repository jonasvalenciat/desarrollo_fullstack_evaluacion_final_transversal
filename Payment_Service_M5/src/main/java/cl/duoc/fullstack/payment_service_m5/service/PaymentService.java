package cl.duoc.fullstack.payment_service_m5.service;

import cl.duoc.fullstack.payment_service_m5.dto.PaymentRequest;
import cl.duoc.fullstack.payment_service_m5.dto.PaymentResponse;
import cl.duoc.fullstack.payment_service_m5.exception.PaymentBusinessException;
import cl.duoc.fullstack.payment_service_m5.model.Payment;
import cl.duoc.fullstack.payment_service_m5.model.PaymentStatus;
import cl.duoc.fullstack.payment_service_m5.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000.00");

    private final PaymentRepository paymentRepository;

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Procesando pago para la orden ID: {}", request.orderId());

        validateBusinessRules(request);

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago procesado exitosamente con ID: {}", savedPayment.getId());
        return toResponse(savedPayment);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentBusinessException(
                        "Pago no encontrado con ID: " + id, "PAYMENT_NOT_FOUND"));
        return toResponse(payment);
    }

    private void validateBusinessRules(PaymentRequest request) {
        if (request.orderId() == null || request.orderId() <= 0) {
            throw new PaymentBusinessException(
                    "El ID de la orden debe ser un número positivo",
                    "INVALID_ORDER_ID");
        }

        if (request.amount() == null || request.amount() <= 0) {
            throw new PaymentBusinessException(
                    "El monto debe ser mayor a $0.01",
                    "INVALID_AMOUNT");
        }

        if (BigDecimal.valueOf(request.amount()).compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new PaymentBusinessException(
                    "Transacción rechazada: el monto excede el límite de $1.000.000. Transacción sospechosa.",
                    "AMOUNT_EXCEEDS_LIMIT");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getCreatedAt());
    }
}
