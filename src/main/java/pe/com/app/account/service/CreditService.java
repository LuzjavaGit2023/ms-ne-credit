package pe.com.app.account.service;

import pe.com.app.account.common.config.DocumentType;
import pe.com.app.account.controller.request.CreditNewRequest;
import pe.com.app.account.controller.request.CreditUpdateRequest;
import pe.com.app.account.controller.request.PaymentRequest;
import pe.com.app.account.controller.request.ConsumptionRequest;
import pe.com.app.account.controller.response.CreditNewResponse;
import pe.com.app.account.controller.response.CreditResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <b>Interface</b>: CreditService <br/>
 * <b>Copyright</b>: 2025 Tu Banco - Celula <br/>
 * .
 *
 * @author 2025 Tu Banco - Peru <br/>
 * <u>Service Provider</u>: Tu Banco <br/>
 * <u>Changes:</u><br/>
 * <ul>
 * <li>
 * May 10, 2025 Creación de Clase.
 * </li>
 * </ul>
 */
public interface CreditService {


    Mono<CreditNewResponse> newCredit(CreditNewRequest obj);

    Flux<CreditResponse> getAllCreditsByDocument(DocumentType documentType, String documentNumber);

    Mono<CreditResponse> getCreditsCreditId(String creditId);

    Mono<Void> updateCredit(String creditId, CreditUpdateRequest obj);


    Mono<Void> deleteCredit(String creditId);
    Mono<Void> payCredit(String creditId, PaymentRequest payment);

    Mono<Void> consumeCredit(String creditId, ConsumptionRequest consumption);

}
