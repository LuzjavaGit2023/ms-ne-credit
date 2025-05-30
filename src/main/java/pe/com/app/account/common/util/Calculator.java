package pe.com.app.account.common.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import pe.com.app.account.common.config.DeadLineToReturn;
import pe.com.app.account.model.dto.credit.FeeDto;

@Slf4j
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
        final double monthlyRate = annualRate / 100 / 12;

        if (monthlyRate == 0) {
            return principal / months; // Sin interés
        }

        return principal * (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);
    }

    public static int getTermInMonths(DeadLineToReturn termDeadLineToReturn) {
        var monthText = "";
        switch (termDeadLineToReturn) {
            case MONTHS_6:
            case MONTHS_12:
            case MONTHS_18:
                monthText = termDeadLineToReturn.getDescription().split("_")[1];
                log.info("mes identificado para Tiempo de Retorno, Key {}, value {}",
                        termDeadLineToReturn.getDescription(), monthText );
                return Integer.parseInt(monthText); // meses
            default:
                monthText = termDeadLineToReturn.getDescription().split("_")[1];
                log.info("mes identificado para Tiempo de Retorno, Key {}, value {}",
                        termDeadLineToReturn.getDescription(), monthText );
                return Integer.parseInt(monthText) * 12; // anios
        }
    }

    public static List<FeeDto> calculateFeeList(int months, Double amount, LocalDate startDate) {
        final List<FeeDto> list = new ArrayList<>();
        for (int i = 1 ; i <= months ; i++) {
            final var nextPaymentDate = startDate.plusMonths(i);
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
        final double tasaMensual = tasaAnual / 100 / 12;
        final double cuota = monto * (tasaMensual * Math.pow(1 + tasaMensual, plazoMeses)) /
                (Math.pow(1 + tasaMensual, plazoMeses) - 1);

        double saldoPendiente = monto;

        for (int i = 1; i <= cuotasPagadas; i++) {
            final double interesMes = saldoPendiente * tasaMensual;
            final double abonoCapital = cuota - interesMes;
            saldoPendiente -= abonoCapital;
        }

        return saldoPendiente;
    }

    public static String generateNumber3DigitsOnBase64() {
        final Random rand = new Random();

        // Generar número entre 100 y 999
        final int numero = rand.nextInt(900) + 100;

        // Convertir a String y codificar en Base64
        final String numeroStr = String.valueOf(numero);
        final String base64Encoded = Base64.getEncoder().encodeToString(numeroStr.getBytes());

        return base64Encoded;
    }

    public static String generateNumber4DigitsOnBase64() {
        final Random rand = new Random();

        // Generar número aleatorio entre 1000 y 9999 (4 dígitos)
        final int numero = rand.nextInt(9000) + 1000;

        // Convertir a String y codificar en Base64
        final String numeroStr = String.valueOf(numero);
        final String base64Encoded = Base64.getEncoder().encodeToString(numeroStr.getBytes());

        return base64Encoded;
    }
}
