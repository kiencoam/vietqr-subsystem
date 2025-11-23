package itss.demo.vietqr.vietqrsubsystem;

import org.springframework.stereotype.Service;

import itss.demo.vietqr.IQRPayment;
import itss.demo.vietqr.dto.PaymentStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VietQRSubsystemController implements IQRPayment {

    private final VietQRService vietQRService;

    @Override
    public String generateQR(int orderId, long amount) {
        return vietQRService.generateQR(orderId, amount);
    }

    @Override
    public PaymentStatus getPaymentStatus(int orderId) {
        var transaction = vietQRService.getTransactionByOrderId(orderId);
        if (transaction != null) {
            PaymentStatus status = transaction.toPaymentStatus();
            return status;
        } else {
            PaymentStatus status = new PaymentStatus();
            status.setStatus(false);
            return status;
        }
    }

}
