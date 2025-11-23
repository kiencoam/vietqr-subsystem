package itss.demo.vietqr;

import org.springframework.web.bind.annotation.*;

import itss.demo.vietqr.dto.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/test")
public class TestController {

    private final IQRPayment qrPayment;

    @GetMapping("/payment-status/{orderId}")
    public PaymentStatus testGetPaymentStatus(@PathVariable String orderId) {
        return qrPayment.getPaymentStatus(Integer.parseInt(orderId));
    }

    @GetMapping("/generate-qr")
    public String testGenerateQR(@RequestParam String orderId, @RequestParam String amount) {
        return qrPayment.generateQR(Integer.parseInt(orderId), Long.parseLong(amount));
    }

}
