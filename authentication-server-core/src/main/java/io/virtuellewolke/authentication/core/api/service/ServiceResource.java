package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.UpdateAuthorityRequest;
import io.virtuellewolke.authentication.core.api.model.UpdateServiceRequest;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.spring.security.annotations.AdminResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RequestMapping(value = "/api", produces = "application/json")
@Tag(name = "Authy - Service Resource")
public interface ServiceResource {
    @AdminResource
    @RequestMapping(path = "/services", method = RequestMethod.GET)
    ResponseEntity<List<Service>> listServices();

    @AdminResource
    @RequestMapping(path = "/identity/{id}/services", method = RequestMethod.GET)
    ResponseEntity<List<Service>> listServicesUserHasAccessTo(@PathVariable("id") Integer userId);

    @AdminResource
    @RequestMapping(path = "/services", method = RequestMethod.POST)
    ResponseEntity<Service> createService(@RequestBody Service identity);

    @AdminResource
    @RequestMapping(path = "/service/{id}", method = RequestMethod.PATCH)
    ResponseEntity<Service> updateService(@PathVariable("id") Integer id, @RequestBody UpdateServiceRequest request);

    @AdminResource
    @RequestMapping(path = "/service/{id}", method = RequestMethod.DELETE)
    void deleteService(@PathVariable("id") Integer id);

    @AdminResource
    @RequestMapping(path = "/service/{id}", method = RequestMethod.GET)
    ResponseEntity<Service> getService(@PathVariable("id") Integer id);
}