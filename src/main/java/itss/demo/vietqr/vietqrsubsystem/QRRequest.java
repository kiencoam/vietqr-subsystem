package itss.demo.vietqr.vietqrsubsystem;

import lombok.Data;

@Data
public class QRRequest {

    private int orderId;

    private long amount;

    private String cancelUrl;

    private String returnUrl;

    QRRequest(int orderId, long amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.cancelUrl = "https://itss.vn/cancel";
        this.returnUrl = "https://itss.vn/return";
    }

}
