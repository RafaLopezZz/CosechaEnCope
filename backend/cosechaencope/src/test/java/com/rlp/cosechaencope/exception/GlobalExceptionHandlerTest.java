package com.rlp.cosechaencope.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFoundException_returnsNotFoundProblemDetail() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Producto no encontrado");
        ProblemDetail result = exceptionHandler.handleResourceNotFoundException(ex);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Recurso no encontrado");
        assertThat(result.getDetail()).isEqualTo("Producto no encontrado");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleEmailAlreadyExistsException_returnsConflictProblemDetail() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email duplicado");
        ProblemDetail result = exceptionHandler.handleEmailAlreadyExistsException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Conflicto de recursos");
        assertThat(result.getDetail()).isEqualTo("Email duplicado");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleCategoryNameAlreadyExistsException_returnsConflictProblemDetail() {
        CategoryNameAlreadyExistsException ex = new CategoryNameAlreadyExistsException("Categoría duplicada");
        ProblemDetail result = exceptionHandler.handleCategoryNameAlreadyExistsException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Conflicto de categoría");
        assertThat(result.getDetail()).isEqualTo("Categoría duplicada");
    }

    @Test
    void handleStockInsuficienteException_returnsConflictProblemDetail() {
        StockInsuficienteException ex = new StockInsuficienteException("No hay stock suficiente");
        ProblemDetail result = exceptionHandler.handleStockInsuficienteException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Stock insuficiente");
        assertThat(result.getDetail()).isEqualTo("No hay stock suficiente");
    }

    @Test
    void handleInvalidPasswordException_returnsUnauthorizedProblemDetail() {
        InvalidPasswordException ex = new InvalidPasswordException("Contraseña incorrecta");
        ProblemDetail result = exceptionHandler.handleInvalidPasswordException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("Credenciales inválidas");
        assertThat(result.getDetail()).isEqualTo("Contraseña incorrecta");
    }

    @Test
    void handleUnauthorizedException_returnsForbiddenProblemDetail() {
        UnauthorizedException ex = new UnauthorizedException("No autorizado");
        ProblemDetail result = exceptionHandler.handleUnauthorizedException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("Acceso denegado");
        assertThat(result.getDetail()).isEqualTo("No autorizado");
    }

    @Test
    void handleFileStorageException_returnsInternalServerErrorProblemDetail() {
        FileStorageException ex = new FileStorageException("Error subiendo archivo S3");
        ProblemDetail result = exceptionHandler.handleFileStorageException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Error de almacenamiento");
        assertThat(result.getDetail()).isEqualTo("Error subiendo archivo S3");
    }

    @Test
    void handleIllegalArgumentException_returnsBadRequestProblemDetail() {
        IllegalArgumentException ex = new IllegalArgumentException("Parámetro inválido");
        ProblemDetail result = exceptionHandler.handleIllegalArgumentException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Parámetros inválidos");
        assertThat(result.getDetail()).isEqualTo("Parámetro inválido");
    }
}
