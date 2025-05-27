package pe.com.app.account.model.dto.product;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommissionDto implements Serializable {
    private Boolean free;
    private Double cost;
}
