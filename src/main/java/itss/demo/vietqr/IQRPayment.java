package itss.demo.vietqr;

import itss.demo.vietqr.dto.PaymentStatus;

public interface IQRPayment {

    String generateQR(int orderId, long amount);

    PaymentStatus getPaymentStatus(int orderId);

}
