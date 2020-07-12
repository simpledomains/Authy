package io.virtuellewolke.authentication.core.api.remote;


import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.virtuellewolke.authentication.core.api.model.LoginRequest;

public interface AuthyRemoteClient {
    @RequestLine("POST /cas/login?service={service}")
    @Headers({
            "Content-Type: application/json"
    })
    void login(LoginRequest loginRequest, @Param("service") String serviceUrl);


}
