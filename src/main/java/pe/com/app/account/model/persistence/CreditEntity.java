package pe.com.app.account.model.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.com.app.account.common.config.CreditStatus;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.common.config.Currency;
import pe.com.app.account.model.dto.credit.CreditCardDto;
import pe.com.app.account.model.dto.credit.IndividualLoanDto;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "credits")
public class CreditEntity implements Serializable {

    private static final long serialVersionUID = -6734174993200993742L;
    @Id
    private String id;
    private String productId;
    private String clientId;
    private CreditType creditType; // PERSONAL_LOAN BUSINESS_LOAN CREDIT_CARD
    private Currency currency; // Moneda (USD, PEN, EUR, etc.)

    private CreditStatus status; // ESTADO (APROBADO, EN_PROCESO, RECHAZADO, VIGENTE, CANCELADO, VENCIDO)

    private IndividualLoanDto individualLoan; // credito por persona, empresa
    private CreditCardDto creditCard; // tarjeta de crédito

    //Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
