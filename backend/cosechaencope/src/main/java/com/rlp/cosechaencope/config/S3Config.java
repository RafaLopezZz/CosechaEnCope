package com.rlp.cosechaencope.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuración para AWS S3.
 * 
 * <p>Esta configuración establece el cliente S3 y las propiedades necesarias
 * para la gestión de archivos en Amazon S3.</p>
 * 
 * @author rafalopezzz
 */
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3Config {

    /**
     * Clave de acceso AWS
     */
    private String accessKey;

    /**
     * Clave secreta AWS
     */
    private String secretKey;

    /**
     * Región de AWS
     */
    private String region;

    /**
     * Nombre del bucket S3
     */
    private String bucketName;

    /**
     * URL base del bucket S3
     */
    private String baseUrl;

    /**
     * Crea y configura el cliente S3.
     *
     * @return Cliente S3 configurado
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}