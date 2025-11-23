package itss.demo.vietqr.dto;

import lombok.Data;

@Data
public class PaymentStatus {

    private String transactionId;

    private boolean status;

    private int orderId;

    private long amount;

    public boolean success() {
        return status;
    }

}
