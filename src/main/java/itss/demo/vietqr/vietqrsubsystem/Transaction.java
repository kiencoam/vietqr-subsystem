package itss.demo.vietqr.vietqrsubsystem;

import itss.demo.vietqr.dto.PaymentStatus;
import lombok.Data;

@Data
class Transaction {
    private String _id;
    private String transactionid;
    private String transactiontime;
    private String referencenumber;
    private int amount;
    private String content;
    private String bankaccount;
    private String orderId;
    private int orderCode;
    private String sign;
    private String terminalCode;
    private String urlLink;
    private String serviceCode;
    private String subTerminalCode;
    private String reftransactionid;
    private String counterAccountName;
    private String counterAccountNumber;
    private String counterAccountBankId;
    private String counterAccountBankName;
    private String virtualAccountNumber;
    private String virtualAccountName;
    private String currency;
    private String paymentLinkId;
    private String createdAt;
    private String updatedAt;

    PaymentStatus toPaymentStatus() {
        PaymentStatus status = new PaymentStatus();
        status.setTransactionId(this.transactionid);
        status.setOrderId(this.orderCode);
        status.setStatus(true);
        status.setAmount(this.amount);
        return status;
    }
}
