package cl.duoc.fullstack.payment_service_m5.service;

import cl.duoc.fullstack.payment_service_m5.dto.PaymentRequest;
import cl.duoc.fullstack.payment_service_m5.dto.PaymentResponse;
import cl.duoc.fullstack.payment_service_m5.model.Payment;
import cl.duoc.fullstack.payment_service_m5.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Procesando pago para la orden ID: {}", request.orderId());

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setAmount(request.amount());
        payment.setStatus("SUCCESS");

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago procesado exitosamente con ID: {}", savedPayment.getId());
        return toResponse(savedPayment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }
}
