package pe.com.app.account.controller.request;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.DeadLineToReturn;

/**
 * <b>class</b>: IndividualLoanRequest <br/>
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
public class IndividualLoanRequest implements Serializable {

    private static final long serialVersionUID = -3388630192398518074L;
    private Double amount; // Monto otorgado al cliente
    private DeadLineToReturn termDeadLineToReturn; //plazo acordado en meses o anios
    // private PaymentFrequency paymentFrequency; // no se considero variable, para todos sera mensual

}
