package itss.demo.vietqr.paypalsubsystem;

import lombok.Data;

@Data
class PayPalResponse {

    private String id;
    private String intent;
    private String status;
    private Links[] links;

    @Data
    static class Links {
        private String href;
        private String rel;
        private String method;
    }

}
