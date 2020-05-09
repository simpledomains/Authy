package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.database.entity.ClientAuthCert;
import io.virtuellewolke.authentication.core.spring.security.annotations.AdminResource;
import io.virtuellewolke.authentication.core.spring.security.annotations.AuthorizedResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.List;

@RequestMapping(value = "/api", produces = "application/json")
@Tag(name = "Authy - X509 User Certificates Resource")
public interface X509Resource {
    @AdminResource
    @RequestMapping(path = "/user/{id}/certificates", method = RequestMethod.GET)
    ResponseEntity<List<ClientAuthCert>> listCertificates(HttpServletRequest request, @PathVariable("id") Integer id);

    @AdminResource
    @RequestMapping(path = "/user/{id}/certificates", method = RequestMethod.POST)
    ResponseEntity<ByteArrayResource> issueCertificate(HttpServletRequest request, @PathVariable("id") Integer id, @RequestParam("deviceName") String deviceName);

    @AdminResource
    @RequestMapping(path = "/user/{id}/certificate/{serial}", method = RequestMethod.DELETE)
    ResponseEntity<Void> revokeCertificate(HttpServletRequest request, @PathVariable("id") Integer id, @PathVariable("serial") BigInteger serial);

    @AuthorizedResource
    @RequestMapping(path = "/session/me/certificates", method = RequestMethod.GET)
    ResponseEntity<List<ClientAuthCert>> listMyCertificates(HttpServletRequest request);

    @AuthorizedResource
    @RequestMapping(path = "/session/me/certificates", method = RequestMethod.POST)
    ResponseEntity<ByteArrayResource> issueMyCertificate(HttpServletRequest request, @RequestParam("deviceName") String deviceName);

    @AuthorizedResource
    @RequestMapping(path = "/session/me/certificate/{serial}", method = RequestMethod.DELETE)
    ResponseEntity<Void> revokeMyCertificate(HttpServletRequest request, @PathVariable("serial") BigInteger serial);
}