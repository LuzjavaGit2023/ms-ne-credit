package pe.com.app.account.webclient;

import pe.com.app.account.controller.request.ConsumptionRequest;
import pe.com.app.account.controller.request.PaymentRequest;
import pe.com.app.account.model.dto.transaction.TransactionResponseDto;
import reactor.core.publisher.Mono;

public interface TransactionClient {

    Mono<TransactionResponseDto> savePayment(String creditId, PaymentRequest deposit);

    Mono<TransactionResponseDto> saveConsumption(String creditId, ConsumptionRequest consumption);

}
