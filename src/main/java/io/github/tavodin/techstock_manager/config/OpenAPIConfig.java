package io.github.tavodin.techstock_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hello Swagger OpenAPI")
                        .version("v1")
                        .description("Some description")
                        .termsOfService("https://www.erudio.com.br/blog/")
                        .license(new License().name("Apache 2.0").url("https://www.erudio.com.br/blog/")));
    }
}
