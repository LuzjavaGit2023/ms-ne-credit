package pe.com.app.account.common.util;

public class Constant {
    public static final String ERROR_CODE = "Error on Service";
    public static final String ELEMENT_NOT_FOUND = "Producto Crediticio no encontrada con el id indicado";
    public static final String ELEMENT_NOT_ACTIVE = "Producto Crediticio actualmente esta inactivo";
    public static final String ELEMENT_IS_USED = "Producto Crediticio ya esta en uso o con pagos";

    public static final String T_CRED_FIELDS_UPDATE = "En Tarj. Credito, necesita billingDay y isContactlessEnabled";
    public static final String LOAN_FIELDS_UPDATE = "En Prestamo necesita campos(amount, termDeadLineToReturn)";

    public static final String PN_HAS_ONE_LOAN = "El cliente(persona natural) ya tiene 1 credito, no procede.";
    public static final String IDENTIFY_VISA = "4921-23"; // EMISOR VISA
    public static final String IDENTIFY_VISA_TEXT = "VISA"; // EMISOR VISA
    public static final String BANK_TEXT = "BANCO_NACION"; // EMISOR VISA
    public static final Double ANNUAL_INTEREST = 24d; // annual interest 24%

}
