package bookdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel="settlements", path="settlements")
public interface SettlementRepository extends PagingAndSortingRepository<Settlement, Long>{    
    Optional<Settlement> findByOrderId(Long orderId);
}
