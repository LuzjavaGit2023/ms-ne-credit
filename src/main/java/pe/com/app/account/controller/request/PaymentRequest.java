package pe.com.app.account.controller.request;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.Currency;

/**
 * <b>class</b>: PaymentRequest <br/>
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
public class PaymentRequest implements Serializable {
    private static final long serialVersionUID = 6513467778668045250L;
    private Double amount;
    private Currency currency; // Moneda (USD, PEN, EUR, etc.)

    private String entityClient;
    private UUID feeId;
}
