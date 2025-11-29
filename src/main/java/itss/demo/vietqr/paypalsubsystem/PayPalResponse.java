package itss.demo.vietqr.paypalsubsystem;

import lombok.Data;

@Data
public class PayPalResponse {

    private String id;
    private String intent;
    private String status;
    private Links[] links;

    @Data
    public static class Links {
        private String href;
        private String rel;
        private String method;
    }

}
