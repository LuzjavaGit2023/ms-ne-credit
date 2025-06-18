package pe.com.app.account.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.com.app.account.common.config.ClientType;
import pe.com.app.account.common.config.CreditStatus;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.common.config.DocumentType;
import pe.com.app.account.common.config.FeeStatusType;
import pe.com.app.account.common.mapper.CreditMapper;
import pe.com.app.account.common.util.Constant;
import pe.com.app.account.controller.request.ConsumptionRequest;
import pe.com.app.account.controller.request.CreditNewRequest;
import pe.com.app.account.controller.request.CreditUpdateRequest;
import pe.com.app.account.controller.request.PaymentRequest;
import pe.com.app.account.controller.response.CreditNewResponse;
import pe.com.app.account.controller.response.CreditResponse;
import pe.com.app.account.model.dto.client.ClientDto;
import pe.com.app.account.model.dto.product.ProductDto;
import pe.com.app.account.model.persistence.CreditEntity;
import pe.com.app.account.repository.CreditRepository;
import pe.com.app.account.webclient.ClientClient;
import pe.com.app.account.webclient.ProductClient;
import pe.com.app.account.webclient.TransactionClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private final TransactionClient transactionClient;

    @Override
    public Mono<CreditNewResponse> newCredit(CreditNewRequest obj) {
        log.info("newCredit ::::::::::::::: execute, request {}", obj);
        return productClient.getProduct(obj.getProductId())
                .flatMap(this::validateCorrectProduct)
                .flatMap(productDto -> clientClient.getClient(obj.getClientId())
                        .flatMap(clientDto -> validateCorrectClient(productDto, clientDto))
                        .flatMap(clientDto ->
                                validateRules(productDto, clientDto)
                                        .flatMap(aBoolean -> saveNewCreditValidated(obj, clientDto, productDto))
                                )
                );
    }

    private Mono<ClientDto> validateCorrectClient(ProductDto product, ClientDto client) {

        final var creditType = CreditType.fromString(product.getProductSubType());

        log.info("current creditType : {}", creditType);

        if (client.getClientType() == ClientType.NATURAL && creditType.equals(CreditType.BUSINESS_LOAN)) {
            return Mono.error(buildException("El producto no puede ser tomado por una Persona natural, no procede."));

        }
        if (client.getClientType() == ClientType.BUSINESS && creditType.equals(CreditType.PERSONAL_LOAN)) {
            return Mono.error(buildException("El producto no puede ser tomado por una Empresa, no procede."));

        }
        return Mono.just(client);
    }

    private Mono<CreditNewResponse> saveNewCreditValidated(CreditNewRequest obj, ClientDto client, ProductDto product) {
        log.info("saveNewCreditValidated, nueva credit product {}", obj);
        return Mono.just(CreditMapper.buildCreditEntityNew(obj, client, product))
                .flatMap(this::assignCardNumber)
                .flatMap(creditEntity -> repository.save(creditEntity))
                .map(creditEntity -> {
                    log.info("resultado del registro {}", creditEntity);
                    return CreditNewResponse.builder()
                            .id(creditEntity.getId())
                            .productId(creditEntity.getProductId())
                            .clientId(creditEntity.getClientId())

                            .cardNumber(creditEntity.getCreditCard() == null ? null :
                                    creditEntity.getCreditCard().getCardNumber())
                            .creditType(creditEntity.getCreditType())
                            .fixedRate(creditEntity.getCreditType().equals(CreditType.CREDIT_CARD) ? null :
                                    creditEntity.getIndividualLoan().getFixedRate())
                            .schedule(creditEntity.getCreditType().equals(CreditType.CREDIT_CARD) ? null :
                                    creditEntity.getIndividualLoan().getSchedule())

                            .expirationDate(creditEntity.getCreditCard() == null ? null :
                                    creditEntity.getCreditCard().getExpirationDate())
                            .securityCode(creditEntity.getCreditCard() == null ? null :
                                    creditEntity.getCreditCard().getSecurityCode())
                            .securityKey(creditEntity.getCreditCard() == null ? null :
                                    creditEntity.getCreditCard().getSecurityKey())

                            .build();
                });
    }

    private Mono<CreditEntity> assignCardNumber(CreditEntity creditEntity) {
        final StringBuilder cardNumber = new StringBuilder("");
        switch (creditEntity.getCreditType()) {
            case CREDIT_CARD :

                final LocalDateTime horaActual = LocalDateTime .now();
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-ddHH-mmss");
                final String random = horaActual.format(formatter);
                cardNumber.append(Constant.IDENTIFY_VISA).append(random);

                log.info("assignCardNumber : numero de tarjeta asignada : " + cardNumber);

                creditEntity.getCreditCard().setCardNumber(cardNumber.toString());

                break;
            default : log.info("assignCardNumber : No es necesario asignar numero de tarjeta");
        }
        return Mono.just(creditEntity);
    }


    @Override
    public Flux<CreditResponse> getAllCreditsByDocument(DocumentType documentType, String documentNumber) {
        log.info("getAllCreditsByDocument ::::::::::::::: execute, documentType {}, documentNumber {} ",
                documentType, documentNumber);
        return clientClient.getClientByDocument(documentType, documentNumber)
                .flatMapMany(clientDto -> repository.findByClientId(clientDto.getId())
                        .map(CreditMapper::buildCreditResponse));

    }

    @Override
    public Mono<CreditResponse> getCreditId(String creditId) {
        log.info("getCreditId ::::::::::::::: execute, creditId {}", creditId);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(buildException(Constant.ELEMENT_NOT_FOUND)))
                .map(CreditMapper::buildCreditResponse)
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
        log.info("updateCredit ::::::::::::::: execute, creditId {}, request {}", creditId, obj);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_FOUND)))
                .flatMap(creditEntity -> validateCorrectInput(creditEntity, obj))
                .flatMap(creditEntity -> {
                    if (CreditStatus.INACTIVO.equals(creditEntity.getStatus())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_ACTIVE));
                    }
                    if (CreditStatus.VIGENTE.equals(creditEntity.getStatus())
                            && !CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {
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

    @Override
    public Mono<Void> deleteCredit(String creditId) {
        log.info("deleteCredit ::::::::::::::: execute, creditId {}", creditId);
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
    public Mono<Void> savePayment(String creditId, PaymentRequest payment) {
        log.info("savePayment ::::::::::::::: execute, creditId {}, request {}", creditId, payment);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_FOUND)))
                .flatMap(creditEntity -> {
                    log.info("CreditType : {}", creditEntity.getCreditType());
                    if (CreditStatus.INACTIVO.equals(creditEntity.getStatus())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_ACTIVE));
                    }
                    if (payment.getCurrency() == null) {
                        payment.setCurrency(creditEntity.getCurrency());
                    }
                    //registrar transaccion
                    //adicionar saldo, liberar saldo disponible
                    return validateAvailablePayment(creditEntity, payment)
                            .flatMap(paymentRequest -> executePayment(creditId, creditEntity, paymentRequest));
                }).then();
    }

    @Override
    public Mono<Void> saveConsumption(String creditId, ConsumptionRequest consumption) {
        log.info("saveConsumption ::::::::::::::: execute, creditId {}, request {}", creditId, consumption);
        return repository.findById(creditId)
                .switchIfEmpty(Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_FOUND)))
                .flatMap(creditEntity -> {
                    log.info("CreditType : {}", creditEntity.getCreditType());
                    if (CreditStatus.INACTIVO.equals(creditEntity.getStatus())) {
                        return Mono.error(new IllegalStateException(Constant.ELEMENT_NOT_ACTIVE));
                    }
                    if (consumption.getCurrency() == null) {
                        consumption.setCurrency(creditEntity.getCurrency());
                    }
                    //validar si hay saldo disponible
                    //decrementar, consumir saldo disponible y registrar transaccion
                    return validateAvailableConsumption(creditEntity, consumption)
                            .flatMap(consumptionRequest ->
                                    executeConsumption(creditId, creditEntity, consumptionRequest));
                })
                .then();
    }

    public static IllegalStateException buildException(String txt) {
        return new IllegalStateException(txt);
    }

    private Mono<CreditEntity> validateCorrectInput(CreditEntity creditEntity, CreditUpdateRequest obj) {
        if (CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {
            if (obj.getBillingDay() == null || obj.getIsContactlessEnabled() == null) {
                return Mono.error(buildException(Constant.T_CRED_FIELDS_UPDATE));
            }
        } else {
            if (obj.getAmount() == null || obj.getTermDeadLineToReturn() == null) {
                return Mono.error(buildException(Constant.LOAN_FIELDS_UPDATE));
            }
        }
        return Mono.just(creditEntity);
    }

    private Mono<CreditEntity> executePayment(String creditId, CreditEntity creditEntity,
                                              PaymentRequest paymentRequest) {
        return transactionClient.savePayment(creditId, paymentRequest)
                .flatMap(transactionResponseDto -> {
                    log.info("Payment saved with id : {}", transactionResponseDto.getId());

                    if (CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {

                        log.info("Pago como TARDEJTA DE CREDITO");
                        final Double currentAvailableCredit = creditEntity.getCreditCard().getAvailableCredit();
                        final Double currentBalance = creditEntity.getCreditCard().getCurrentBalance();
                        final Double newCurrentBalance = currentBalance + paymentRequest.getAmount();

                        log.info("CurrentAvailableCredit : {}", currentAvailableCredit);
                        log.info("CurrentBalance : {}", currentBalance);
                        log.info("New CurrentBalance : {}", newCurrentBalance);
                        log.info("Amount : {}", paymentRequest.getAmount());

                        creditEntity.getCreditCard().setCurrentBalance(newCurrentBalance);
                        return repository.save(creditEntity).doOnNext(creditEntity1 -> log.info("TC, balance updated"));

                    } else { // prestamo personal o negocio, involucra documento de pago, de una cuota

                        log.info("Pago como PRESTAMO");
                        return changeFeeDocToPaid(creditEntity, paymentRequest)
                                .flatMap(creditEntityUpdated -> repository.save(creditEntityUpdated))
                                .doOnNext(creditEntity1 -> log.info("Payment Document updated"));

                    }
                });
    }

    private Mono<CreditEntity> changeFeeDocToPaid(CreditEntity creditEntity, PaymentRequest paymentRequest) {
        return creditEntity.getIndividualLoan().getSchedule()
                .stream()
                .filter(feeDto -> feeDto.getUuid().equals(paymentRequest.getFeeId()))
                .findFirst()
                .map(feeDoc -> {
                    feeDoc.setStatus(FeeStatusType.PAID);
                    feeDoc.setPaidDate(LocalDate.now());
                    log.info("Documento de pago : {}", feeDoc);
                    return Mono.just(creditEntity);
                })
                .orElseGet(() -> Mono.error(buildException("No existe documento indicado para el pago , no procede")));

    }

    private Mono<CreditEntity> executeConsumption(String creditId, CreditEntity creditEntity,
                                                  ConsumptionRequest consumptionRequest) {
        return transactionClient.saveConsumption(creditId, consumptionRequest)
                .flatMap(transactionResponseDto -> {
                    log.info("Consumption saved with id : {}", transactionResponseDto.getId());
                    final Double currentAvailableCredit = creditEntity.getCreditCard().getAvailableCredit();
                    final Double currentBalance = creditEntity.getCreditCard().getCurrentBalance();
                    final Double newCurrentBalance = currentBalance + consumptionRequest.getAmount();

                    log.info("CurrentAvailableCredit : {}", currentAvailableCredit);
                    log.info("CurrentBalance : {}", currentBalance);
                    log.info("New AvailableCredit : {}", newCurrentBalance);
                    log.info("Amount : {}", consumptionRequest.getAmount());

                    creditEntity.getCreditCard().setCurrentBalance(newCurrentBalance);
                    return repository.save(creditEntity).doOnNext(creditEntity1 -> log.info("Balance updated"));
                });
    }


    private Mono<PaymentRequest> validateAvailablePayment(CreditEntity creditEntity, PaymentRequest payment) {
        final Double amount = payment.getAmount();
        if (amount <= 0.0) {
            log.info("validateAvailablePayment: el monto debe ser mayor a cero");
            return Mono.error(buildException("En Pago, el monto debe ser mayor a cero, no procede."));
        }
        if (!CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {
            //estamos en prestamo (personal o empresa), validar datos de Doc de Pago
            if (payment.getFeeId() == null) {
                log.info("validateAvailablePayment: En Pago prestamo, debe ingresar el Doc. de Pago Id");
                return Mono.error(buildException("En Pago prestamo, debe ingresar el Doc. de Pago Id, no procede."));
            }

            return creditEntity.getIndividualLoan().getSchedule()
                    .stream()
                    .filter(feeDto -> feeDto.getUuid().equals(payment.getFeeId()))
                    .findFirst()
                    .map(feeDoc -> {
                        log.info("Documento de pago {}", feeDoc);
                        if (FeeStatusType.PAID.equals(feeDoc.getStatus())) {
                            return Mono.<PaymentRequest>error(
                                    buildException("En Pago prestamo, el documento de pago ya esta pagado, no procede.")
                            );
                        } else if (!feeDoc.getAmount().equals(payment.getAmount())) {
                            return Mono.<PaymentRequest>error(
                                    buildException("El monto de pago para el documento es "
                                            + creditEntity.getCurrency() + " "
                                            + feeDoc.getAmount()
                                            + ", no procede.")
                            );
                        }
                        log.info("Montos y campos requeridos, validados como PRESTAMO");
                        return Mono.just(payment);
                    })
                    .orElseGet(() -> {
                        log.info("validateAvailablePayment: No existe documento de pago con ese ID");
                        return Mono.error(buildException("No existe documento de pago con ese ID, no procede."));
                    });
        } else {
            log.info("Montos y campos requeridos, validados como TARJ CRED");
            return Mono.just(payment);
        }
    }

    private Mono<ConsumptionRequest> validateAvailableConsumption(CreditEntity creditEntity,
                                                                  ConsumptionRequest consumption) {
        final Double currentBalance = creditEntity.getCreditCard().getCurrentBalance();
        final Double amount = consumption.getAmount();
        if (amount <= 0.0) {
            log.info("validateAvailableConsumption: el monto debe ser mayor a cero");
            return Mono.error(buildException("En consumo, el monto debe ser mayor a cero, no procede."));
        }
        else if ( (currentBalance - amount) < 0.0) {
            log.info("validate: no hay saldo disponible para la operacion[saldo {}]", currentBalance);
            return Mono.error(buildException("En consumo, no hay saldo disponible para la operacion, no procede."));
        }
        return Mono.just(consumption);
    }

    private Mono<ProductDto> validateCorrectProduct(ProductDto productDto) {
        final boolean valid = Arrays.stream(CreditType.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(productDto.getProductSubType()));
        log.info("Is product valid to account : {} on {}", valid, productDto.getProductSubType());
        if (valid) {
            return Mono.just(productDto);
        }
        return Mono.error(buildException("Producto seleccionado no es valido para credito, no procede."));
    }

    private Mono<Boolean> validateRules(ProductDto product, ClientDto client) {
        log.info("validateRules : start product : {}", product);
        log.info("validateRules : start client : {}", client);
        log.info("validateRules : ClientType : {}", client.getClientType());
        if (client.getClientType() == ClientType.NATURAL) {
            log.info("Cliente PERSONAL, puede tener solo 1 credito por persona");
            return repository.countByClientIdAndCreditType(client.getId(), CreditType.PERSONAL_LOAN)
                    .flatMap(countCPL -> {
                        log.info("validateRules : countByClientIdAndCreditType : {}", countCPL);
                        if (countCPL + isOneMoreCurrentPersonalLoan(product) > 1) {
                            return Mono.error(buildException(Constant.PN_HAS_ONE_LOAN));
                        }
                        return Mono.just(true);
                    });
        }
        else if (client.getClientType() == ClientType.BUSINESS) {
            log.info("Cliente BUSINESS, puede tener mas de 1 credito por empresa");
        } else {
            return Mono.error(buildException("Tipo de cliente no identifiado, no procede."));
        }
        return Mono.just(true);
    }

    private int isOneMoreCurrentPersonalLoan(ProductDto product) {
        return CreditType.PERSONAL_LOAN.getDescription().equals(product.getProductSubType()) ? 1 : 0 ;
    }

}
