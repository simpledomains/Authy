package de.reynok.authentication.core.database.repository;

import de.reynok.authentication.core.database.entity.Service;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceRepository extends CrudRepository<Service, Integer> {
    List<Service> findAll();
}