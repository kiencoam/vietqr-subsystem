package itss.demo.vietqr.paypalsubsystem;

import itss.demo.vietqr.CreditCardService;
import itss.demo.vietqr.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardServiceImpl implements CreditCardService {

        private final WebClient webClient;
        private final PayPalConfig payPalConfig;

        private PayPalToken generateToken() {
                // Implement OAuth 2.0 token generation logic here
                String username = payPalConfig.getClientId();
                String password = payPalConfig.getClientSecret();

                return webClient.post()
                                .uri(payPalConfig.getBaseUrl() + "/v1/oauth2/token")
                                .headers(headers -> headers.setBasicAuth(username, password))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                                .retrieve()
                                .bodyToMono(PayPalToken.class)
                                .block();
        }

        @Override
        public PaymentStatusResult checkoutPayment(BigDecimal amount, String returnUrl, String cancelUrl) {
                PayPalToken token = generateToken();
                String accessToken = token.getAccessToken();

                PayPalRequest request = PayPalRequest.builder()
                                .intent("CAPTURE")
                                .paymentSource(
                                                PayPalRequest.PaymentSource.builder()
                                                                .paypal(
                                                                                PayPalRequest.PaymentSource.Paypal
                                                                                                .builder()
                                                                                                .experienceContext(
                                                                                                                PayPalRequest.PaymentSource.Paypal.ExperienceContext
                                                                                                                                .builder()
                                                                                                                                .returnUrl(returnUrl)
                                                                                                                                .cancelUrl(cancelUrl)
                                                                                                                                .userAction("PAY_NOW")
                                                                                                                                .build())
                                                                                                .build())
                                                                .build())
                                .purchaseUnits(new PayPalRequest.PurchaseUnits[] {
                                                PayPalRequest.PurchaseUnits.builder()
                                                                .amount(
                                                                                PayPalRequest.PurchaseUnits.Amount
                                                                                                .builder()
                                                                                                .currencyCode("USD")
                                                                                                .value(amount.toString())
                                                                                                .build())
                                                                .build()
                                })
                                .build();

                PayPalResponse response = webClient.post()
                                .uri(payPalConfig.getBaseUrl() + "/v2/checkout/orders")
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<PayPalResponse>() {
                                })
                                .block();

                String payUrl = "";

                if (response == null) {
                        throw new PaymentException("Response from PayPal is null");
                }

                for (PayPalResponse.Links link : response.getLinks()) {
                        if ("payer-action".equals(link.getRel())) {
                                payUrl = link.getHref();
                                break;
                        }
                }

                if (payUrl.isEmpty()) {
                        throw new PaymentException("Payment URL not found in PayPal response");
                }

                return new PaymentStatusResult(
                                response.getId(),
                                response.getStatus(),
                                payUrl);
        }

        @Override
        public PaymentStatusResult getPaymentStatus(String paymentId) {

                PayPalToken token = generateToken();
                String accessToken = token.getAccessToken();

                PayPalResponse response = webClient.get()
                                .uri(payPalConfig.getBaseUrl() + "/v2/checkout/orders/{paymentId}", paymentId)
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<PayPalResponse>() {
                                })
                                .block();

                if (response == null) {
                        throw new PaymentException("Response from PayPal is null");
                }

                return new PaymentStatusResult(
                                response.getId(),
                                response.getStatus(),
                                null);
        }
}
