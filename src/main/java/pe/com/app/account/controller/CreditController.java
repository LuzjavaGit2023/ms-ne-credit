package pe.com.app.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.com.app.account.advice.ErrorResponse;
import pe.com.app.account.common.config.DocumentType;
import pe.com.app.account.controller.request.CreditNewRequest;
import pe.com.app.account.controller.request.CreditUpdateRequest;
import pe.com.app.account.controller.request.PaymentRequest;
import pe.com.app.account.controller.request.ConsumptionRequest;
import pe.com.app.account.controller.response.CreditNewResponse;
import pe.com.app.account.controller.response.CreditResponse;
import pe.com.app.account.service.CreditService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <b>Class</b>: CreditController <br/>
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
@RestController
@RequiredArgsConstructor
@RequestMapping("/credits")
@Tag(name = "Credits", description = "Functional operations related to credits")
public class CreditController {

    private final CreditService service;

    /**
     * This method is used to create a new credit element.
     *
     * @return Void Mono.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "This method is used to create a new credit element.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<CreditNewResponse> newCredit(@RequestBody CreditNewRequest request) {
        return service.newCredit(request);
    }

    /**
     * This method is used to list all credit elements of client by document.
     *
     * @return CreditResponse Flux.
     */
    @GetMapping("/{documentType}/{documentNumber}")
    @Operation(summary = "This method is used to list all credit elements of client by document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Flux<CreditResponse> getAllCreditsByDocument(@PathVariable DocumentType documentType, @PathVariable String documentNumber) {
        return service.getAllCreditsByDocument(documentType, documentNumber);
    }

    /**
     * This method is used to get a credit element of client by number Account.
     *
     * @return AccountResponse Mono.
     */
    @GetMapping("/{creditId}")
    @Operation(summary = "This method is used to get a credit element of client by number Account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<CreditResponse> getCreditByNumberAccount(@PathVariable String creditId) {
        return service.getCreditsCreditId(creditId);
    }

    /**
     * This method is used to update a credit element.
     *
     * @return Void Mono.
     */
    @PatchMapping("/{creditId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "This method is used to update a credit element.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<Void> updateCredit(@PathVariable String creditId, @RequestBody CreditUpdateRequest obj) {
        return service.updateCredit(creditId, obj);
    }

    /**
     * This method is used to delete a credit element.
     *
     * @return Void Mono.
     */
    @DeleteMapping("/{creditId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "This method is used to delete a credit element.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<Void> deleteCredit(@PathVariable String creditId) {

        return service.deleteCredit(creditId);
    }

    /**
     * This method is used to save a payment to credit element.
     *
     * @return Void Mono.
     */
    @PostMapping("/{creditId}/payment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "This method is used to save a payment to credit element.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<Void> payCredit(@PathVariable String creditId, @RequestBody PaymentRequest deposit) {
        return service.payCredit(creditId, deposit);
    }

    /**
     * This method is used to save a consumption to an account element.
     *
     * @return Void Mono.
     */
    @PostMapping("/{creditId}/consumption")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "This method is used to save a consumption to an account element.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<Void> consumeCredit(@PathVariable String creditId, @RequestBody ConsumptionRequest consumption) {
        return service.consumeCredit(creditId, consumption);
    }
}
