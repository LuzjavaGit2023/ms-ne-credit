package pe.com.app.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.com.app.account.common.config.ClientType;
import pe.com.app.account.common.config.CreditStatus;
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
                        .flatMap(clientDto -> validateCorrectClient(productDto, clientDto))
                        .flatMap(clientDto ->
                                validateRules(productDto, clientDto, obj)
                                        .flatMap(aBoolean -> saveNewCreditValidated(obj, clientDto, productDto))
                                )
                );
    }

    private Mono<ClientDto> validateCorrectClient(ProductDto product, ClientDto client) {

        var creditType = CreditType.fromString(product.getProductSubType());

        log.info("current creditType : {}", creditType);

        if(client.getClientType() == ClientType.NATURAL && creditType.equals(CreditType.BUSINESS_LOAN)) {
            return Mono.error(new IllegalStateException("El producto no puede ser tomado por una Persona natural, no procede."));

        }
        if(client.getClientType() == ClientType.BUSINESS && creditType.equals(CreditType.PERSONAL_LOAN)) {
            return Mono.error(new IllegalStateException("El producto no puede ser tomado por una Empresa, no procede."));

        }
        return Mono.just(client);
    }

    private Mono<CreditNewResponse> saveNewCreditValidated(CreditNewRequest obj, ClientDto client, ProductDto product) {
        log.info("saveNewCreditValidated, nueva credit product {}", obj);
        return Mono.just(CreditMapper.buildCreditEntityNew(obj, client, product))
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

                            .expirationDate(creditEntity.getCreditCard() == null ? null : creditEntity.getCreditCard().getExpirationDate())
                            .securityCode(creditEntity.getCreditCard() == null ? null : creditEntity.getCreditCard().getSecurityCode())
                            .securityKey(creditEntity.getCreditCard() == null ? null : creditEntity.getCreditCard().getSecurityKey())

                            .build();
                });
    }

    private Mono<CreditEntity> assignCardNumber(CreditEntity creditEntity, ClientDto client, ProductDto product) {
        StringBuilder cardNumber = new StringBuilder("");
        switch (creditEntity.getCreditType()) {
            case CREDIT_CARD :

                LocalDateTime horaActual = LocalDateTime .now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-ddHH-mmss");
                String random = horaActual.format(formatter);
                cardNumber.append(Constant.IDENTIFY_VISA + random);

                log.info("assignCardNumber : numero de tarjeta asignada : " + cardNumber);

                creditEntity.getCreditCard().setCardNumber(cardNumber.toString());

                break;
            default : log.info("assignCardNumber : No es necesario asignar numero de tarjeta");
        }
        return Mono.just(creditEntity);
    }


    @Override
    public Flux<CreditResponse> getAllCreditsByDocument(DocumentType documentType, String documentNumber) {
        log.info("getAllCreditsByDocument : execute, documentType {}, documentNumber {} ", documentType, documentNumber);
        return clientClient.getClientByDocument(documentType, documentNumber)
                .flatMapMany(clientDto -> repository.findByClientId(clientDto.getId())
                        .map(creditEntity -> CreditMapper.buildCreditResponse(creditEntity)));

    }

    @Override
    public Mono<CreditResponse> getCreditId(String creditId) {
        log.info("getCreditId : execute, creditId {}", creditId);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_FOUND)))
                .flatMap(creditEntity -> {
                    if (CreditStatus.INACTIVO.equals(creditEntity.getStatus())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_ACTIVE));
                    }
                    return Mono.just(creditEntity);
                })
                .map(creditEntity -> CreditMapper.buildCreditResponse(creditEntity))
                .flatMap(creditResponse -> {
                    if (CreditType.CREDIT_CARD.equals(creditResponse.getCreditType())) {
                        creditResponse.getCreditCard().setSecurityCode(null);
                        creditResponse.getCreditCard().setSecurityKey(null);
                    }
                    return Mono.just(creditResponse);
                });
    }

    @Override
    public Mono<CreditResponse> updateCredit(String creditId, CreditUpdateRequest obj) {
        log.info("updateCredit : execute, creditId {}, request {}", creditId, obj);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_FOUND)))
                .flatMap(creditEntity -> validateCorrectInput(creditEntity, obj))
                .flatMap(creditEntity -> {
                    if (CreditStatus.INACTIVO.equals(creditEntity.getStatus())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_ACTIVE));
                    }
                    if (CreditStatus.VIGENTE.equals(creditEntity.getStatus()) && !CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_IS_USED));
                    }
                    return Mono.just(creditEntity);
                })
                .flatMap(creditEntity -> repository.save(CreditMapper.buildEntityUpdate(creditEntity, obj)))
                .map(creditEntity -> {
                    log.info("registro de Credito modificado : {}", creditEntity);
                    return CreditMapper.buildCreditResponse(creditEntity);
                })
                .flatMap(creditResponse -> {
                    if (CreditType.CREDIT_CARD.equals(creditResponse.getCreditType())) {
                        creditResponse.getCreditCard().setSecurityCode(null);
                        creditResponse.getCreditCard().setSecurityKey(null);
                    }
                    return Mono.just(creditResponse);
                });
    }

    private Mono<CreditEntity> validateCorrectInput(CreditEntity creditEntity, CreditUpdateRequest obj) {
        if (CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {
            if (obj.getBillingDay() == null || obj.getIsContactlessEnabled() == null) {
                return Mono.error(new IllegalStateException("Para actualizar Tarjeta de Credito necesita enviar los campos(billingDay, isContactlessEnabled), no procede."));
            }
        } else {
            if (obj.getAmount() == null || obj.getTermDeadLineToReturn() == null) {
                return Mono.error(new IllegalStateException("Para actualizar Prestamo necesita enviar los campos(amount, termDeadLineToReturn), no procede."));
            }
        }
        return Mono.just(creditEntity);
    }

    @Override
    public Mono<Void> deleteCredit(String creditId) {
        log.info("deleteCredit : execute, creditId {}", creditId);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_FOUND)))
                .flatMap(creditEntity -> {
                    if (CreditStatus.INACTIVO.equals(creditEntity.getStatus())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_ACTIVE));
                    }
                    return repository.save(CreditMapper.buildEntityDelete(creditEntity));
                }).then();
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
