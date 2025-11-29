package itss.demo.vietqr;

import lombok.Getter;

import java.math.BigDecimal;

public interface CreditCardService {

    public PaymentStatusResult checkoutPayment(BigDecimal amount, String returnUrl, String cancelUrl);

    public PaymentStatusResult getPaymentStatus(String paymentId);

    @Getter
    class PaymentStatusResult {
        private final String paymentId;
        private final PaymentStatus status;
        private final String payUrl;

        public PaymentStatusResult(String paymentId, String status, String payUrl) {
            this.paymentId = paymentId;
            this.status = PaymentStatus.valueOf(status);
            this.payUrl = payUrl;
        }

    }

    enum PaymentStatus {
        APPROVED,
        PAYER_ACTION_REQUIRED
    }

}
