package pe.com.app.account.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.com.app.account.common.config.CreditType;
import pe.com.app.account.model.persistence.CreditEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <b>Interface</b>: CreditRepository <br/>
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
public interface CreditRepository extends ReactiveMongoRepository<CreditEntity, String> {

    Mono<Long> countByClientIdAndCreditType(String id, CreditType creditType);

    Flux<CreditEntity> findByClientId(String clientId);
}
