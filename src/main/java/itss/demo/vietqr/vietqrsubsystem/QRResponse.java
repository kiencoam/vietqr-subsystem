package itss.demo.vietqr.vietqrsubsystem;

import lombok.Data;

@Data
public class QRResponse {

    private String qrCode;

    private String checkoutUrl;

    private String paymentLinkId;

    private int orderCode;

}
