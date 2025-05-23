package pe.com.app.account.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.CreditStatus;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.common.config.Currency;

/**
 * <b>class</b>: CreditNewRequest <br/>
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
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreditNewRequest {
    private String productId;
    private String clientId;
    private Currency currency; // Moneda (USD, PEN, EUR, etc.)

    private CreditStatus status; // ESTADO (APROBADO, EN_PROCESO, RECHAZADO, VIGENTE, CANCELADO, VENCIDO)

    private IndividualLoanRequest individualLoan; // credito por persona
    private CreditCardRequest creditCard; // tarjeta de crédito
}
