package itss.demo.vietqr;

import org.springframework.web.bind.annotation.*;

import itss.demo.vietqr.dto.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/test")
public class TestController {

    private final IQRPayment qrPayment;
    private final CreditCardService creditCardService;

    @GetMapping("/payment-status/{orderId}")
    public PaymentStatus testGetPaymentStatus(@PathVariable String orderId) {
        return qrPayment.getPaymentStatus(Integer.parseInt(orderId));
    }

    @GetMapping("/generate-qr")
    public String testGenerateQR(@RequestParam String orderId, @RequestParam String amount) {
        return qrPayment.generateQR(Integer.parseInt(orderId), Long.parseLong(amount));
    }

    @GetMapping("/credit-card/payment/{paymentId}")
    public CreditCardService.PaymentStatusResult testGetCreditCardPaymentStatus(@PathVariable String paymentId) {
        return creditCardService.getPaymentStatus(paymentId);
    }

    @GetMapping("/credit-card/checkout")
    public CreditCardService.PaymentStatusResult testCheckoutCreditCardPayment(@RequestParam String amount, @RequestParam String returnUrl, @RequestParam String cancelUrl) {
        return creditCardService.checkoutPayment(BigDecimal.valueOf(Long.parseLong(amount)), returnUrl, cancelUrl);
    }
}
