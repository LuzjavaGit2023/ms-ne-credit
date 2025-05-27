package pe.com.app.account.common.mapper;

import java.time.LocalDateTime;
import pe.com.app.account.common.config.CreditStatus;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.common.config.DeadLineToReturn;
import pe.com.app.account.common.config.PaymentFrequency;
import pe.com.app.account.common.util.Calculator;
import pe.com.app.account.common.util.Constant;
import pe.com.app.account.controller.request.CreditNewRequest;
import pe.com.app.account.controller.request.CreditUpdateRequest;
import pe.com.app.account.controller.response.CreditResponse;
import pe.com.app.account.model.dto.IndividualReferencedDto;
import pe.com.app.account.model.dto.client.ClientDto;
import pe.com.app.account.model.dto.credit.CreditCardDto;
import pe.com.app.account.model.dto.credit.IndividualLoanDto;
import pe.com.app.account.model.dto.product.ProductDto;
import pe.com.app.account.model.persistence.CreditEntity;

public class CreditMapper {

    public static CreditEntity buildCreditEntityNew(CreditNewRequest request, ClientDto client, ProductDto productDto) {

        IndividualLoanDto loan = null;
        CreditCardDto credit = null;
        final var nowTime = LocalDateTime.now();

        final var currentCreditType = CreditType.fromString(productDto.getProductSubType());

        if (CreditType.PERSONAL_LOAN.equals(currentCreditType) || CreditType.BUSINESS_LOAN.equals(currentCreditType)) {

            final var p = request.getIndividualLoan();
            loan = buildIndividualLoanDto(p.getAmount(), PaymentFrequency.MONTHLY, p.getTermDeadLineToReturn());

        }
        if (CreditType.CREDIT_CARD.equals(currentCreditType)) {

            final var c = request.getCreditCard();

            credit = CreditCardDto.builder()
                    .cardNumber(null)
                    .cardHolder(c.getCardHolder() != null ? c.getCardHolder() :
                            IndividualReferencedDto.builder()
                                    .documentType(client.getDocumentType())
                                    .documentNumber(client.getDocumentNumber())
                                    .name(client.getName())
                                    .lastName(client.getLastName())
                                    .build())
                    .expirationDate(nowTime.plusYears(5).toLocalDate())
                    .securityCode(Calculator.generateNumber3DigitsOnBase64())
                    .securityKey(Calculator.generateNumber4DigitsOnBase64())
                    .currentBalance(c.getAvailableCredit())
                    .availableCredit(c.getAvailableCredit())
                    .minimumPaymentDue(0d)
                    .lastPaymentAmount(0d)
                    .lastPaymentDate(null)
                    .billingDay(c.getBillingDay())
                    .isContactlessEnabled(c.getIsContactlessEnabled())
                    .cardBrand(c.getCardBrand())
                    .cardCreditType(c.getCardCreditType())
                    .issuerBank(Constant.BANK_TEXT)
                    .build();
        }

        return CreditEntity.builder()
                .productId(productDto.getId())
                .clientId(request.getClientId())
                .creditType(CreditType.fromString(productDto.getProductSubType()))
                .currency(request.getCurrency())
                .status(request.getStatus())
                .individualLoan(loan)
                .creditCard(credit)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static CreditResponse buildCreditResponse(CreditEntity creditEntity) {
        return CreditResponse.builder()
                .id(creditEntity.getId())
                .productId(creditEntity.getProductId())
                .clientId(creditEntity.getClientId())
                .creditType(creditEntity.getCreditType())
                .currency(creditEntity.getCurrency())

                .individualLoan(creditEntity.getIndividualLoan())
                .creditCard(creditEntity.getCreditCard())

                .build();
    }

    public static CreditEntity buildEntityDelete(CreditEntity creditEntity) {
        creditEntity.setStatus(CreditStatus.INACTIVO);
        creditEntity.setUpdatedAt(LocalDateTime.now());
        return creditEntity;
    }

    public static CreditEntity buildEntityUpdate(CreditEntity creditEntity, CreditUpdateRequest obj) {
        if (CreditType.CREDIT_CARD.equals(creditEntity.getCreditType())) {
            creditEntity.getCreditCard().setBillingDay(obj.getBillingDay());
            creditEntity.getCreditCard().setIsContactlessEnabled(obj.getIsContactlessEnabled());
        } else {
            final var p = creditEntity.getIndividualLoan();
            final var loan = buildIndividualLoanDto(obj.getAmount(),
                    p.getPaymentFrequency(), obj.getTermDeadLineToReturn());
            creditEntity.setIndividualLoan(loan);
        }
        creditEntity.setUpdatedAt(LocalDateTime.now());
        return creditEntity;
    }

    public static IndividualLoanDto buildIndividualLoanDto(Double amount,
                                                           PaymentFrequency paymentFrequency,
                                                           DeadLineToReturn termDeadLineToReturn
    ) {

        final var nowTime = LocalDateTime.now();
        final var termInMonths = Calculator.getTermInMonths(termDeadLineToReturn);

        //cuota fija
        var fixedFee = Calculator.calculateMonthlyPayment(
                amount
                , Constant.ANNUAL_INTEREST
                , termInMonths
        );
        fixedFee = Math.round(fixedFee * 100.0) / 100.0;

        final var schedule = Calculator.calculateFeeList(termInMonths, fixedFee, nowTime.toLocalDate());

        return IndividualLoanDto.builder()
                .amount(amount)
                .termInMonths(termInMonths)
                .annualInterestRate(Constant.ANNUAL_INTEREST)
                .fixedRate(true) // set como tasa fija
                .disbursementDate(nowTime.plusDays(1))
                .startDate(nowTime)
                .endDate(schedule.get(schedule.size() - 1).getPaymentDate().atStartOfDay()) //ultimo fecha de pago
                .outstandingBalance(Calculator.calcularSaldoPendiente(amount,
                        Constant.ANNUAL_INTEREST, termInMonths, 0))
                .totalInstallments(schedule.size())
                .installmentsPaid(0) //ninguna couta pagada
                .paymentFrequency(paymentFrequency)
                .fixedMonthlyPayment(fixedFee)
                .schedule(schedule)
                .build();
    }
}
