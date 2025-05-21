package pe.com.app.account.model.dto.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FeeDto {
    private Integer order;
    private UUID uuid;
    private LocalDate paymentDate;
    private Double amount;
}
