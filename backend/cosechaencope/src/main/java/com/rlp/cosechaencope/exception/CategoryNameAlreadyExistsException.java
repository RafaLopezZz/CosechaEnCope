package com.rlp.cosechaencope.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta crear o actualizar una categoría con un nombre que ya existe.
 * 
 * @author rafalopezzz
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CategoryNameAlreadyExistsException extends RuntimeException {

    public CategoryNameAlreadyExistsException(String message) {
        super(message);
    }

    public CategoryNameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}