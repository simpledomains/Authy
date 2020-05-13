package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.UpdateServiceRequest;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ServiceResourceImpl implements ServiceResource {

    private final ServiceRepository  serviceRepository;
    private final IdentityRepository identityRepository;

    @Override
    public ResponseEntity<List<Service>> listServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }

    @Override
    public ResponseEntity<List<Service>> listServicesUserHasAccessTo(Integer userId) {
        Identity identity = identityRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

        List<Service> services = serviceRepository.findAll()
                .stream()
                .filter(service -> service.isIdentityAllowed(identity))
                .collect(Collectors.toList());

        return ResponseEntity.ok(services);
    }

    @Override
    public ResponseEntity<Service> createService(Service service) {
        service.setId(null);
        serviceRepository.save(service);
        return ResponseEntity.ok(service);
    }

    @Override
    public ResponseEntity<Service> updateService(Integer id, UpdateServiceRequest request) {
        Service service = serviceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        serviceRepository.save(request.update(service));
        return ResponseEntity.ok(service);
    }

    @Override
    public void deleteService(Integer id) {
        serviceRepository.deleteById(id);
    }

    @Override
    public ResponseEntity<Service> getService(Integer id) {
        return ResponseEntity.ok(serviceRepository.findById(id).orElseThrow(EntityNotFoundException::new));
    }
}
