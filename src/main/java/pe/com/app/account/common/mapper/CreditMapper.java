package pe.com.app.account.common.mapper;

import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.common.util.Calculator;
import pe.com.app.account.common.util.Constant;
import pe.com.app.account.controller.request.CreditNewRequest;
import pe.com.app.account.model.dto.credit.CreditCardDto;
import pe.com.app.account.model.dto.credit.IndividualLoanDto;
import pe.com.app.account.model.dto.product.ProductDto;
import pe.com.app.account.model.persistence.CreditEntity;

import java.time.LocalDateTime;

public class CreditMapper {

    public static CreditEntity buildCreditEntityNew(CreditNewRequest request, ProductDto productDto){

        IndividualLoanDto loan = null;
        CreditCardDto credit = null;

        if (request.getIndividualLoan() != null) {

            var p = request.getIndividualLoan();

            //calculo para prestamo de 1 anio
            var nowTime = LocalDateTime.now();
            var termInMonths = Calculator.getTermInMonths(p.getTermDeadLineToReturn());

            //cuota fija
            var fixedFee = Calculator.calculateMonthlyPayment(
                    p.getAmount()
                    , Constant.ANNUAL_INTEREST
                    , termInMonths
            );
            fixedFee = Math.round(fixedFee * 100.0) / 100.0;

            var schedule = Calculator.calculateFeeList(termInMonths, fixedFee, nowTime.toLocalDate());

            loan = IndividualLoanDto.builder()
                    .amount(p.getAmount())
                    .termInMonths(termInMonths)
                    .annualInterestRate(Constant.ANNUAL_INTEREST)
                    .fixedRate(true) // set como tasa fija
                    .disbursementDate(nowTime.plusDays(1))
                    .startDate(nowTime)
                    .endDate(schedule.get(schedule.size() - 1).getPaymentDate().atStartOfDay()) //ultimo fecha de pago
                    .outstandingBalance(Calculator.calcularSaldoPendiente(p.getAmount(), Constant.ANNUAL_INTEREST, termInMonths, 0))
                    .totalInstallments(schedule.size())
                    .installmentsPaid(0) //ninguna couta pagada
                    .paymentFrequency(p.getPaymentFrequency())
                    .fixedMonthlyPayment(fixedFee)
                    .schedule(schedule)
                    .build();
        }
        if (request.getCreditCard() != null) {
            credit = CreditCardDto.builder()

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
}
