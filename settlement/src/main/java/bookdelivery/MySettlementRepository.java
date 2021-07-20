package bookdelivery;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.List;
import java.util.Optional;



//@RepositoryRestResource(collectionResourceRel="Mysettlements", path="Mysettlements")
public interface MySettlementRepository extends CrudRepository<MySettlement, Long> {
    Optional<MySettlement> findByOrderId(Long orderId);
}
