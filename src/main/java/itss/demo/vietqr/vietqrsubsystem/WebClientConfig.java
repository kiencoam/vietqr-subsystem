package itss.demo.vietqr.vietqrsubsystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;

@Configuration
class WebClientConfig {

    @Getter
    private final String baseUrl = "https://vietqr-callback-server.onrender.com";

    @Getter
    private final String username = "kiencoam";

    @Getter
    private final String password = "1392004kien";

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }
}