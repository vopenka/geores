package net.petrvopenka.geores.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import net.petrvopenka.geores.entity.StreetEntity;

@RepositoryRestResource
public interface StreetRepository extends PagingAndSortingRepository<StreetEntity, Long> {

	public List<StreetEntity> findByNameContaining(String name);

}
