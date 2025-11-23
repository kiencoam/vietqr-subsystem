package itss.demo.vietqr.vietqrsubsystem;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import itss.demo.vietqr.exception.PaymentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class VietQRService {

    private final WebClient webClient;
    private final WebClientConfig webClientConfig;

    private VietQRToken generateToken() {

        String username = webClientConfig.getUsername();
        String password = webClientConfig.getPassword();

        return webClient.post()
                .uri(webClientConfig.getBaseUrl() + "/vqr/api/token_generate")
                .headers(headers -> headers.setBasicAuth(username, password))
                .retrieve()
                .bodyToMono(VietQRToken.class)
                .block();
    }

    Transaction getTransactionByOrderId(int orderId) {

        VietQRToken token = generateToken();

        var response = webClient.get()
                .uri(webClientConfig.getBaseUrl() + "/app/api/transaction?orderId=" + orderId)
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<VietQRResponse<Transaction>>() {})
                .block();

        if (response != null && !response.isError()) {
            return response.getObject();
        } else {
            return null;
        }
    }

    String generateQR(int orderId, long amount) {

        VietQRToken token = generateToken();

        QRRequest qrRequest = new QRRequest(orderId, amount);

        var response = webClient.post()
                .uri(webClientConfig.getBaseUrl() + "/qr/api/generate_qr")
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .bodyValue(qrRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<VietQRResponse<QRResponse>>() {})
                .block();

        if (response == null) {
            throw new PaymentException("No response from VietQR service");
        } else if (response.isError()) {
            throw new PaymentException(response.getErrorReason());
        } else if (response.getObject() == null) {
            throw new PaymentException("No QR code generated");
        }

        return response.getObject().getQrCode();

    }

}