package itss.demo.vietqr.paypalsubsystem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class PayPalRequest {

    private String intent;

    @JsonProperty("payment_source")
    private PaymentSource paymentSource;

    @JsonProperty("purchase_units")
    private PurchaseUnits[] purchaseUnits;

    @Data
    @Builder
    static class PaymentSource {
        private Paypal paypal;

        @Data
        @Builder
        static class Paypal {

            @JsonProperty("experience_context")
            private ExperienceContext experienceContext;

            @Data
            @Builder
            static class ExperienceContext {

                @JsonProperty("return_url")
                private String returnUrl;

                @JsonProperty("cancel_url")
                private String cancelUrl;

                @JsonProperty("user_action")
                private String userAction;
            }
        }
    }

    @Data
    @Builder
    static class PurchaseUnits {
        private Amount amount;

        @Data
        @Builder
        static class Amount {

            @JsonProperty("currency_code")
            private String currencyCode;

            @JsonProperty("value")
            private String value;
        }
    }


}
