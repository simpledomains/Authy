package io.virtuellewolke.authentication.core.api.remote;

import feign.Feign;
import feign.Request;
import feign.okhttp.OkHttpClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.ConnectionSpec;
import okhttp3.TlsVersion;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Setter
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthyRemoteClientBuilder {

    private String url;
    private Long   timeout;

    public static AuthyRemoteClientBuilder builder() {
        return new AuthyRemoteClientBuilder();
    }

    public AuthyRemoteClient build() {
        Feign.Builder builder = new Feign.Builder();

        ConnectionSpec requireTls12 = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder().connectionSpecs(Arrays.asList(requireTls12))
                .build();

        return builder
                .client(new OkHttpClient(client))
                .options(new Request.Options(timeout, TimeUnit.MILLISECONDS, timeout, TimeUnit.MILLISECONDS, false))
                .target(AuthyRemoteClient.class, url);
    }
}
