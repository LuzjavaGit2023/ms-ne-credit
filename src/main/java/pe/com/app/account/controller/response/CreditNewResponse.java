package pe.com.app.account.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.model.dto.credit.FeeDto;

import java.time.LocalDate;
import java.util.List;

/**
 * <b>class</b>: CreditNewResponse <br/>
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
public class CreditNewResponse {
    private String id;
    private String productId;
    private String clientId;
    private String cardNumber;
    private CreditType creditType;
    private Boolean fixedRate;
    private List<FeeDto> schedule;

    private LocalDate expirationDate; // Fecha de vencimiento de la tarjeta
    private String securityCode; // CVV encriptado
    private String securityKey; // digital pasword
}
