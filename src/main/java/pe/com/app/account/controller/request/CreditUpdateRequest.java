package pe.com.app.account.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.DeadLineToReturn;
import pe.com.app.account.model.dto.IndividualReferencedDto;

import java.util.List;

/**
 * <b>class</b>: CreditUpdateRequest <br/>
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
public class CreditUpdateRequest {
    private Double amount; // Monto otorgado al cliente
    private DeadLineToReturn termDeadLineToReturn; //plazo acordado en meses o anios

    private Integer billingDay;  // dia corte de facturacion
    private Boolean isContactlessEnabled;  // Si permite pagos sin contacto
}
