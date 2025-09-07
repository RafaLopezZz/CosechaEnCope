package com.rlp.cosechaencope.exception;

/**
 * Excepción personalizada para errores relacionados con el almacenamiento de archivos.
 * 
 * <p>Se lanza cuando ocurren errores durante la subida, eliminación o gestión 
 * de archivos en el sistema de almacenamiento (AWS S3).</p>
 * 
 * @author rafalopezzz
 */
public class FileStorageException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     *
     * @param message Mensaje descriptivo del error
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje de error y una causa.
     *
     * @param message Mensaje descriptivo del error
     * @param cause Causa original del error
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}