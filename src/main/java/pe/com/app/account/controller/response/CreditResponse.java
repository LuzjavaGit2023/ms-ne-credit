package pe.com.app.account.controller.response;

import lombok.*;
import lombok.experimental.SuperBuilder;
import pe.com.app.account.model.dto.credit.CreditLoanDto;

import java.io.Serializable;

/**
 * <b>class</b>: CreditResponse <br/>
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
@Getter
@Setter
@SuperBuilder
public class CreditResponse extends CreditLoanDto implements Serializable {

    private static final long serialVersionUID = -4835095220588061994L;
}
