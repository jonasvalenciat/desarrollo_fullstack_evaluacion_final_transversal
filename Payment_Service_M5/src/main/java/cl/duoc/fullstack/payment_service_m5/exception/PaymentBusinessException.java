package cl.duoc.fullstack.payment_service_m5.exception;

import lombok.Getter;

@Getter
public class PaymentBusinessException extends RuntimeException {

    private final String code;

    public PaymentBusinessException(String message, String code) {
        super(message);
        this.code = code;
    }
}
