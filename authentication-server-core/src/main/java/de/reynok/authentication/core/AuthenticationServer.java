package de.reynok.authentication.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Slf4j
@EnableSwagger2
@EnableScheduling
@SpringBootApplication
public class AuthenticationServer {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServer.class, args);
    }

    @Bean
    public Docket docket() {
        return (new Docket(DocumentationType.SWAGGER_2))
                .select()
                .paths(PathSelectors.regex("/(api|cas)/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfo(
                "Authy - REST Api",
                "A CAS and ForwardAuth Server",
                "1.0.0",
                null, null, null, null, new ArrayList<>()
        );
    }
}