package ytg.projetjavaytg.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API ASTA - Suivi de Tutorats d'Apprentis")
                        .version("1.0")
                        .description("API du projet ASTA (Application de Suivi de Tutorats d'Apprentis)")
                );
    }

    @Bean
    public GroupedOpenApi restApiGroup() {
        return GroupedOpenApi.builder()
                .group("REST API")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi mvcGroup() {
        return GroupedOpenApi.builder()
                .group("MVC Controllers")
                .pathsToExclude("/api/**")
                .packagesToScan("ytg.projetjavaytg.Controllers")
                .build();
    }
}
