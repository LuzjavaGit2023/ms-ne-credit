package pe.com.app.account.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.app.account.common.config.CardType;
import pe.com.app.account.common.config.PaymentFrequency;
import pe.com.app.account.model.dto.IndividualReferencedDto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <b>class</b>: CreditCardRequest <br/>
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
public class CreditCardRequest implements Serializable {
    private static final long serialVersionUID = -1450053396181538375L;

    private IndividualReferencedDto cardHolder; // titular de la tarjeta
    private String securityCode; // CVV encriptado
    private Double availableCredit;  // Saldo credito disponible
    private Integer billingDay;  // dia corte de facturacion
    private Boolean isContactlessEnabled;  // Si permite pagos sin contacto
    private CardType cardBrand; // Visa, Mastercard, AMEX, etc.

}
