package com.rlp.cosechaencope.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Value("${cosechaencope.openapi.dev-url:http://localhost:8081}")
    private String devUrl;

    //@Value("${cosechaencope.openapi.prod-url:https://cosechaencope.up.railway.app}")
    //private String prodUrl;

    @Bean
    public OpenAPI cosechaEnCopeOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Servidor de desarrollo");

        //Server prodServer = new Server();
        //prodServer.setUrl(prodUrl);
        //prodServer.setDescription("Servidor de producción");

        Contact contact = new Contact()
                .name("Soporte Cosecha-en-Cope")
                .email("contacto@rlp.com")
                .url("https://www.rlp.com/contacto");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Cosecha-en-Cope API")
                .version("1.0.0")
                .description("API para la plataforma Cosecha-en-Cope - Gestión de clientes, productores, artículos y pedidos.")
                .termsOfService("https://www.rlp.com/terminos")
                .contact(contact)
                .license(mitLicense);

        return new OpenAPI()
                .servers(List.of(devServer))
                .info(info);
    }

}
