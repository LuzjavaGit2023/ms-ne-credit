package pe.com.app.account.common.util;

import pe.com.app.account.common.config.DeadLineToReturn;
import pe.com.app.account.model.dto.credit.FeeDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Calculator {

    /**
     * Calcula la cuota mensual fija de un prestamo.
     *
     * @param principal   Monto del prestamo
     * @param annualRate  Tasa de interés anual (en porcentaje, ej. 24.0)
     * @param months      Plazo en meses
     * @return            Cuota mensual fija
     */
    public static double calculateMonthlyPayment(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 100 / 12;

        if (monthlyRate == 0) {
            return principal / months; // Sin interés
        }

        return principal * (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);
    }

    public static int getTermInMonths(DeadLineToReturn termDeadLineToReturn) {
        switch (termDeadLineToReturn) {
            case MONTHS_6:
            case MONTHS_12:
            case MONTHS_18:
                return Integer.parseInt(termDeadLineToReturn.getDescription().split("_")[1]); // mese
            default:
                return Integer.parseInt(termDeadLineToReturn.getDescription().split("_")[1]) * 12; // anios
        }
    }

    public static List<FeeDto> calculateFeeList(int months, Double amount, LocalDate startDate) {
        List<FeeDto> list = new ArrayList<>();
        for (int i = 1 ; i <= months ; i++) {
            var nextPaymentDate = startDate.plusMonths(i);
            list.add(
                    FeeDto.builder()
                            .order(i)
                            .uuid(UUID.randomUUID())
                            .amount(amount)
                            .paymentDate(nextPaymentDate)
                            .build()
            );
        }
        return list;
    }

    public static double calcularSaldoPendiente(double monto, double tasaAnual, int plazoMeses, int cuotasPagadas) {
        double tasaMensual = tasaAnual / 100 / 12;
        double cuota = monto * (tasaMensual * Math.pow(1 + tasaMensual, plazoMeses)) /
                (Math.pow(1 + tasaMensual, plazoMeses) - 1);

        double saldoPendiente = monto;

        for (int i = 1; i <= cuotasPagadas; i++) {
            double interesMes = saldoPendiente * tasaMensual;
            double abonoCapital = cuota - interesMes;
            saldoPendiente -= abonoCapital;
        }

        return saldoPendiente;
    }
}
