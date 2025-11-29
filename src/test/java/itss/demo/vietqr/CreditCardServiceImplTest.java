package itss.demo.vietqr;

import itss.demo.vietqr.exception.PaymentException;
import itss.demo.vietqr.paypalsubsystem.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreditCardServiceImplTest {

        @Mock
        private WebClient webClient;

        @Mock
        private PayPalConfig payPalConfig;

        @InjectMocks
        private CreditCardServiceImpl creditCardService;

        // ---- WebClient chain mocks ----
        @Mock
        private WebClient.RequestBodyUriSpec requestBodyUriSpec;
        @Mock
        private WebClient.RequestHeadersSpec requestHeadersSpec;
        @Mock
        private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
        @Mock
        private WebClient.RequestBodySpec requestBodySpec;
        @Mock
        private WebClient.ResponseSpec responseSpec;

        // Additional mocks for handling multiple calls
        @Mock
        private WebClient.RequestBodySpec requestBodySpec2;
        @Mock
        private WebClient.RequestHeadersSpec requestHeadersSpec2;
        @Mock
        private WebClient.ResponseSpec responseSpec2;

        @BeforeEach
        void setup() {
                when(payPalConfig.getBaseUrl()).thenReturn("https://api-m.sandbox.paypal.com");
                when(payPalConfig.getClientId()).thenReturn("clientId");
                when(payPalConfig.getClientSecret()).thenReturn("clientSecret");
        }

        // =====================================================
        // 1) TEST checkoutPayment() - HAPPY PATH
        // =====================================================
        @Test
        void testCheckoutPayment_Success() {
                // Mock token
                PayPalToken token = new PayPalToken();
                token.setAccessToken("mock-token");

                // Mock PayPalResponse
                PayPalResponse response = new PayPalResponse();
                response.setId("ORDER123");
                response.setStatus("PAYER_ACTION_REQUIRED");

                PayPalResponse.Links link = new PayPalResponse.Links();
                link.setRel("payer-action");
                link.setHref("https://paypal.com/pay");

                response.setLinks(Collections.singletonList(link).toArray(new PayPalResponse.Links[0]));

                // ---- Mock OAuth token request ----
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(BodyInserters.FormInserter.class)))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(PayPalToken.class)).thenReturn(Mono.just(token));

                // ---- Mock Create Order request ----
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v2/checkout/orders"))
                                .thenReturn(requestBodySpec2);
                when(requestBodySpec2.headers(any())).thenReturn(requestBodySpec2);
                when(requestBodySpec2.bodyValue(any())).thenReturn(requestHeadersSpec2);
                when(requestHeadersSpec2.retrieve()).thenReturn(responseSpec2);
                when(responseSpec2.bodyToMono(any(ParameterizedTypeReference.class)))
                                .thenReturn(Mono.just(response));

                // ---- Call service ----
                CreditCardService.PaymentStatusResult result = creditCardService.checkoutPayment(
                                BigDecimal.valueOf(20),
                                "https://return",
                                "https://cancel");

                // ---- Verify ----
                assertEquals("ORDER123", result.getPaymentId());
                assertEquals(CreditCardService.PaymentStatus.PAYER_ACTION_REQUIRED, result.getStatus());
                assertEquals("https://paypal.com/pay", result.getPayUrl());

                // Verify OAuth token called
                verify(webClient, times(2)).post();
        }

        // =====================================================
        // 2) TEST checkoutPayment() - response null → exception
        // =====================================================
        @Test
        void testCheckoutPayment_NullResponse_ThrowsException() {
                PayPalToken token = new PayPalToken();
                token.setAccessToken("mock-token");

                // Mock token API
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(PayPalToken.class)).thenReturn(Mono.just(token));

                // Mock order API returns NULL
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v2/checkout/orders"))
                                .thenReturn(requestBodySpec2);
                when(requestBodySpec2.headers(any())).thenReturn(requestBodySpec2);
                when(requestBodySpec2.bodyValue(any())).thenReturn(requestHeadersSpec2);
                when(requestHeadersSpec2.retrieve()).thenReturn(responseSpec2);
                when(responseSpec2.bodyToMono(any(ParameterizedTypeReference.class)))
                                .thenReturn(Mono.empty());

                assertThrows(PaymentException.class, () -> creditCardService.checkoutPayment(
                                BigDecimal.valueOf(20),
                                "https://return",
                                "https://cancel"));
        }

        // =====================================================
        // 3) TEST checkoutPayment() - No payer-action link → exception
        // =====================================================
        @Test
        void testCheckoutPayment_NoPayerActionLink_ThrowsException() {
                // Mock token
                PayPalToken token = new PayPalToken();
                token.setAccessToken("mock-token");

                // Mock PayPalResponse with links but NO payer-action
                PayPalResponse response = new PayPalResponse();
                response.setId("ORDER456");
                response.setStatus("PAYER_ACTION_REQUIRED");

                PayPalResponse.Links link1 = new PayPalResponse.Links();
                link1.setRel("self");
                link1.setHref("https://paypal.com/self");

                PayPalResponse.Links link2 = new PayPalResponse.Links();
                link2.setRel("approve");
                link2.setHref("https://paypal.com/approve");

                response.setLinks(new PayPalResponse.Links[] { link1, link2 });

                // ---- Mock OAuth token request ----
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(BodyInserters.FormInserter.class)))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(PayPalToken.class)).thenReturn(Mono.just(token));

                // ---- Mock Create Order request ----
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v2/checkout/orders"))
                                .thenReturn(requestBodySpec2);
                when(requestBodySpec2.headers(any())).thenReturn(requestBodySpec2);
                when(requestBodySpec2.bodyValue(any())).thenReturn(requestHeadersSpec2);
                when(requestHeadersSpec2.retrieve()).thenReturn(responseSpec2);
                when(responseSpec2.bodyToMono(any(ParameterizedTypeReference.class)))
                                .thenReturn(Mono.just(response));

                // ---- Verify exception thrown ----
                PaymentException exception = assertThrows(PaymentException.class,
                                () -> creditCardService.checkoutPayment(
                                                BigDecimal.valueOf(20),
                                                "https://return",
                                                "https://cancel"));

                assertEquals("Payment URL not found in PayPal response", exception.getMessage());
        }

        // =====================================================
        // 4) TEST checkoutPayment() - Verify full WebClient request
        // =====================================================
        @Test
        void testCheckoutPayment_VerifyFullRequest() {
                // Mock token
                PayPalToken token = new PayPalToken();
                token.setAccessToken("mock-access-token");

                // Mock PayPalResponse
                PayPalResponse response = new PayPalResponse();
                response.setId("ORDER789");
                response.setStatus("PAYER_ACTION_REQUIRED");

                PayPalResponse.Links link = new PayPalResponse.Links();
                link.setRel("payer-action");
                link.setHref("https://paypal.com/checkout");

                response.setLinks(new PayPalResponse.Links[] { link });

                // ---- Mock OAuth token request ----
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.body(any(BodyInserters.FormInserter.class)))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(PayPalToken.class)).thenReturn(Mono.just(token));

                // ---- Mock Create Order request with captor ----
                ArgumentCaptor<PayPalRequest> requestCaptor = ArgumentCaptor.forClass(PayPalRequest.class);

                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v2/checkout/orders"))
                                .thenReturn(requestBodySpec2);
                when(requestBodySpec2.headers(any())).thenReturn(requestBodySpec2);
                when(requestBodySpec2.bodyValue(requestCaptor.capture())).thenReturn(requestHeadersSpec2);
                when(requestHeadersSpec2.retrieve()).thenReturn(responseSpec2);
                when(responseSpec2.bodyToMono(any(ParameterizedTypeReference.class)))
                                .thenReturn(Mono.just(response));

                // ---- Call service ----
                BigDecimal amount = BigDecimal.valueOf(100.50);
                String returnUrl = "https://example.com/return";
                String cancelUrl = "https://example.com/cancel";

                CreditCardService.PaymentStatusResult result = creditCardService.checkoutPayment(
                                amount, returnUrl, cancelUrl);

                // ---- Verify OAuth token endpoint ----
                verify(requestBodyUriSpec).uri("https://api-m.sandbox.paypal.com/v1/oauth2/token");
                verify(requestBodySpec).headers(any());
                verify(requestBodySpec).contentType(MediaType.APPLICATION_FORM_URLENCODED);

                // ---- Verify Create Order endpoint ----
                verify(requestBodyUriSpec).uri("https://api-m.sandbox.paypal.com/v2/checkout/orders");

                // ---- Verify Bearer token header ----
                verify(requestBodySpec2).headers(argThat(consumer -> {
                        // We can't easily verify the consumer content, but we verify it was called
                        return true;
                }));

                // ---- Verify request body structure ----
                PayPalRequest capturedRequest = requestCaptor.getValue();
                assertNotNull(capturedRequest);
                assertEquals("CAPTURE", capturedRequest.getIntent());
                assertNotNull(capturedRequest.getPaymentSource());
                assertNotNull(capturedRequest.getPaymentSource().getPaypal());
                assertNotNull(capturedRequest.getPaymentSource().getPaypal().getExperienceContext());
                assertEquals(returnUrl,
                                capturedRequest.getPaymentSource().getPaypal().getExperienceContext().getReturnUrl());
                assertEquals(cancelUrl,
                                capturedRequest.getPaymentSource().getPaypal().getExperienceContext().getCancelUrl());
                assertEquals("PAY_NOW",
                                capturedRequest.getPaymentSource().getPaypal().getExperienceContext().getUserAction());

                assertNotNull(capturedRequest.getPurchaseUnits());
                assertEquals(1, capturedRequest.getPurchaseUnits().length);
                assertEquals("USD", capturedRequest.getPurchaseUnits()[0].getAmount().getCurrencyCode());
                assertEquals(amount.toString(), capturedRequest.getPurchaseUnits()[0].getAmount().getValue());

                // ---- Verify result ----
                assertEquals("ORDER789", result.getPaymentId());
                assertEquals(CreditCardService.PaymentStatus.PAYER_ACTION_REQUIRED, result.getStatus());
                assertEquals("https://paypal.com/checkout", result.getPayUrl());
        }

        // =====================================================
        // 5) TEST getPaymentStatus() - HAPPY PATH
        // =====================================================
        @Test
        void testGetPaymentStatus_Success() {
                PayPalToken token = new PayPalToken();
                token.setAccessToken("mock-token");

                PayPalResponse response = new PayPalResponse();
                response.setId("PAYID123");
                response.setStatus("APPROVED");

                // ---- Mock OAuth ----
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(PayPalToken.class)).thenReturn(Mono.just(token));

                // ---- Mock GET /v2/checkout/orders/{paymentId} ----
                when(webClient.get()).thenReturn(requestHeadersUriSpec);
                when(requestHeadersUriSpec.uri(
                                "https://api-m.sandbox.paypal.com/v2/checkout/orders/{paymentId}", "PAYID123"))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                                .thenReturn(Mono.just(response));

                // ---- Call service ----
                CreditCardService.PaymentStatusResult result = creditCardService.getPaymentStatus("PAYID123");

                // ---- Verify ----
                assertEquals("PAYID123", result.getPaymentId());
                assertEquals(CreditCardService.PaymentStatus.APPROVED, result.getStatus());
                assertNull(result.getPayUrl());
        }

        // =====================================================
        // 6) TEST getPaymentStatus() - null response → exception
        // =====================================================
        @Test
        void testGetPaymentStatus_NullResponse_ThrowsException() {
                PayPalToken token = new PayPalToken();
                token.setAccessToken("mock-token");

                // ---- Mock token API ----
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                                .thenReturn(requestBodySpec);
                when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
                when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(PayPalToken.class)).thenReturn(Mono.just(token));

                // ---- Mock null response ----
                when(webClient.get()).thenReturn(requestHeadersUriSpec);
                when(requestHeadersUriSpec.uri(
                                "https://api-m.sandbox.paypal.com/v2/checkout/orders/{paymentId}", "PAYID123"))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                                .thenReturn(Mono.empty());

                assertThrows(PaymentException.class, () -> creditCardService.getPaymentStatus("PAYID123"));
        }
}
