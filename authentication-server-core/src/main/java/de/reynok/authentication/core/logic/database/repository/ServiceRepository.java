package de.reynok.authentication.core.logic.database.repository;

import de.reynok.authentication.core.api.models.Service;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceRepository extends CrudRepository<Service, Integer> {
    List<Service> findAll();
}