package de.reynok.authentication.core.backend.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Slf4j
@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "cas.system", name = "api-documentation-exposed")
public class SwaggerConfiguration {

    public SwaggerConfiguration() {
        log.warn("Swagger-API is exposed to the public network. Its served under /v2/api-docs");
    }

    @Bean
    public Docket docket() {
        return (new Docket(DocumentationType.SWAGGER_2))
                .select()
                .paths(PathSelectors.regex("/api/admin/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfo(
                "Authy - REST Api",
                "A CAS and ForwardAuth Server",
                "v1",
                null, null, null, null, new ArrayList<>()
        );
    }
}
