package pe.com.app.account.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CreditStatus {
    APROBADO("APROBADO"),
    EN_PROCESO("EN_PROCESO"),
    RECHAZADO("RECHAZADO"),
    VIGENTE("VIGENTE"),
    CANCELADO("CANCELADO"),
    VENCIDO("VENCIDO");

    private final String description;

    CreditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static CreditStatus fromString(String value) {
        return value != null ? CreditStatus.valueOf(value.toUpperCase()) : null;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
