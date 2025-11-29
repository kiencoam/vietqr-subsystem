package itss.demo.vietqr.paypalsubsystem;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class PayPalConfig {

    @Getter
    @Value("${paypal.client-id}")
    private String clientId;

    @Getter
    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Getter
    @Value("${paypal.base-url}")
    private String baseUrl;

}
