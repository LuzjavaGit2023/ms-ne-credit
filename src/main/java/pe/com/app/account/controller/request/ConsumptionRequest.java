package pe.com.app.account.controller.request;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.Currency;

/**
 * <b>class</b>: ConsumptionRequest <br/>
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
public class ConsumptionRequest implements Serializable {
    private static final long serialVersionUID = -1482580240273914036L;
    private Double amount;
    private Currency currency; // Moneda (USD, PEN, EUR, etc.)
}
