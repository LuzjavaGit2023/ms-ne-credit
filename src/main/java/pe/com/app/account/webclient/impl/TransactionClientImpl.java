package pe.com.app.account.webclient.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pe.com.app.account.advice.ErrorResponse;
import pe.com.app.account.controller.request.ConsumptionRequest;
import pe.com.app.account.controller.request.PaymentRequest;
import pe.com.app.account.model.dto.transaction.TransactionResponseDto;
import pe.com.app.account.webclient.TransactionClient;
import pe.com.app.account.webclient.config.TransactionServiceConfig;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionClientImpl implements TransactionClient {

    @Autowired
    @Qualifier("clientWebToTransaction")
    private WebClient clientWeb;

    private final TransactionServiceConfig config;
    @Override
    public Mono<TransactionResponseDto> savePayment(String creditId, PaymentRequest request) {
        return clientWeb.post()
                .uri(config.getSavePayment(), creditId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> buildError(response))
                .bodyToMono(TransactionResponseDto.class);
    }

    @Override
    public Mono<TransactionResponseDto> saveConsumption(String creditId, ConsumptionRequest request) {
        return clientWeb.post()
                .uri(config.getSaveConsumption(), creditId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> buildError(response))
                .bodyToMono(TransactionResponseDto.class);
    }

    private Mono<IllegalStateException> buildError(ClientResponse response) {
        return response.bodyToMono(ErrorResponse.class)
                .flatMap(errorJson -> Mono.error(
                        new IllegalStateException("Api Transaction, " + errorJson.getMessage().split(":")[1].trim())
                ));
    }
}
