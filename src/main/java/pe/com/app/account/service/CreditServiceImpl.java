package pe.com.app.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.com.app.account.common.config.ClientType;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.common.config.DocumentType;
import pe.com.app.account.common.mapper.CreditMapper;
import pe.com.app.account.common.util.Constant;
import pe.com.app.account.controller.request.CreditNewRequest;
import pe.com.app.account.controller.request.CreditUpdateRequest;
import pe.com.app.account.controller.request.PaymentRequest;
import pe.com.app.account.controller.request.ConsumptionRequest;
import pe.com.app.account.controller.response.CreditNewResponse;
import pe.com.app.account.controller.response.CreditResponse;
import pe.com.app.account.model.dto.client.ClientDto;
import pe.com.app.account.model.dto.product.ProductDto;
import pe.com.app.account.model.persistence.CreditEntity;
import pe.com.app.account.repository.CreditRepository;
import pe.com.app.account.webclient.ClientClient;
import pe.com.app.account.webclient.ProductClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * <b>Class</b>: CreditServiceImpl <br/>
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
@Service
@Slf4j
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository repository;
    private final ClientClient clientClient;
    private final ProductClient productClient;

    @Override
    public Mono<CreditNewResponse> newCredit(CreditNewRequest obj) {
        log.info("newCredit : execute, request {}", obj);
        return productClient.getProduct(obj.getProductId())
                .flatMap(productDto -> validateCorrectProduct(productDto))
                .flatMap(productDto -> clientClient.getClient(obj.getClientId())
                        .flatMap(clientDto ->
                                validateRules(productDto, clientDto, obj)
                                        .flatMap(aBoolean -> saveNewCreditValidated(obj, clientDto, productDto))
                                )
                );
    }

    private Mono<CreditNewResponse> saveNewCreditValidated(CreditNewRequest obj, ClientDto client, ProductDto product) {
        log.info("saveNewCreditValidated, nueva credit product {}", obj);
        return Mono.just(CreditMapper.buildCreditEntityNew(obj, product))
                .flatMap(creditEntity -> assignCardNumber(creditEntity, client, product))
                .flatMap(creditEntity -> repository.save(creditEntity))
                .map(creditEntity -> {
                    log.info("resultado del registro {}", creditEntity);
                    return CreditNewResponse.builder()
                            .id(creditEntity.getId())
                            .productId(creditEntity.getProductId())
                            .clientId(creditEntity.getClientId())
                            .cardNumber(creditEntity.getCreditCard() == null ? null : creditEntity.getCreditCard().getCardNumber())
                            .creditType(creditEntity.getCreditType())
                            .fixedRate(creditEntity.getCreditType().equals(CreditType.CREDIT_CARD) ? null : creditEntity.getIndividualLoan().getFixedRate())
                            .schedule(creditEntity.getCreditType().equals(CreditType.CREDIT_CARD) ? null : creditEntity.getIndividualLoan().getSchedule())
                            .build();
                });
    }

    private Mono<CreditEntity> assignCardNumber(CreditEntity creditEntity, ClientDto client, ProductDto product) {
        StringBuilder numberAccount = new StringBuilder("");
        switch (creditEntity.getCreditType()) {
            case CREDIT_CARD :

                LocalDateTime horaActual = LocalDateTime .now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-ddHH-mmss");
                String random = horaActual.format(formatter);
                numberAccount.append(Constant.IDENTIFY_VISA + random);

                log.info("assignCardNumber : numero de tarjeta asignada : " + numberAccount);

                break;
            default : log.info("assignCardNumber : No es necesario asignar numero de tarjeta");
        }
        return Mono.just(creditEntity);
    }


    @Override
    public Flux<CreditResponse> getAllCreditsByDocument(DocumentType documentType, String documentNumber) {
        log.info("getAllCreditsByDocument : execute, documentType {}, documentNumber {} ", documentType, documentNumber);
        return null;

    }

    @Override
    public Mono<CreditResponse> getCreditsCreditId(String creditId) {
        log.info("getCreditsCreditId : execute, creditId {}", creditId);
        return null;
    }

    @Override
    public Mono<Void> updateCredit(String creditId, CreditUpdateRequest obj) {
        log.info("updateCredit : execute, creditId {}, request {}", creditId, obj);
        return null;
    }

    @Override
    public Mono<Void> deleteCredit(String creditId) {
        log.info("deleteCredit : execute, creditId {}", creditId);
        return null;
    }

    @Override
    public Mono<Void> payCredit(String creditId, PaymentRequest deposit) {
        log.info("payCredit : execute, creditId {}, request {}", creditId, deposit);
        return null;
    }

    @Override
    public Mono<Void> consumeCredit(String creditId, ConsumptionRequest withdrawal) {
        log.info("consumeCredit : execute, creditId {}, request {}", creditId, withdrawal);
        return null;
    }

    private Mono<ProductDto> validateCorrectProduct(ProductDto productDto) {
        boolean valid = Arrays.stream(CreditType.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(productDto.getProductSubType()));
        log.info("Is product valid to account : {} on {}", valid, productDto.getProductSubType());
        if (valid) return Mono.just(productDto);
        return Mono.error(new IllegalStateException("Producto seleccionado no es valido para una cuenta, no procede."));
    }

    private Mono<Boolean> validateRules(ProductDto product, ClientDto client, CreditNewRequest obj) {
        log.info("validateRules : start product : {}", product);
        log.info("validateRules : start client : {}", client);
        log.info("validateRules : ClientType : {}", client.getClientType());
        if(client.getClientType() == ClientType.NATURAL) {
            log.info("Cliente PERSONAL, puede tener solo 1 credito por persona");
            return repository.countByClientIdAndCreditType(client.getId(), CreditType.PERSONAL_LOAN)
                    .flatMap(countCPL -> {
                        log.info("validateRules : countByClientIdAndCreditType : {}", countCPL);
                        if (countCPL + isOneMoreCurrentPersonalLoan(product) > 1) {
                            return Mono.error(new IllegalStateException("El cliente(persona natural) ya tiene 1 credito, no procede."));
                        }
                        return Mono.just(true);
                    });
        }
        else if (client.getClientType() == ClientType.BUSINESS) {
            log.info("Cliente BUSINESS, puede tener mas de 1 credito por empresa");
        } else {
            return Mono.error(new IllegalStateException("Tipo de cliente no identifiado, no procede."));
        }
        return Mono.just(true);
    }

    private int isOneMoreCurrentPersonalLoan(ProductDto product) {
        return CreditType.PERSONAL_LOAN.getDescription().equals(product.getProductSubType()) ? 1 : 0 ;
    }

}
