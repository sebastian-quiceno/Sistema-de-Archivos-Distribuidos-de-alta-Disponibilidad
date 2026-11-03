package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Users.MismatchException;
import com.sdan.middleware.Domain.Exceptions.Users.PasswordNotMatchException;
import com.sdan.middleware.Domain.Exceptions.Users.UnauthorizedUserException;
import com.sdan.middleware.Domain.Exceptions.Users.UserNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameAlreadyExist;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UnauthorizedUserException.class, PasswordNotMatchException.class})
    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({AccessDeniedException.class, FileDoesNotBelongToUserException.class, FolderDoesNotBelongToUserException.class})
    public ResponseEntity<Map<String, Object>> handleForbidden(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UsernameAlreadyExist.class)
    public ResponseEntity<Map<String, Object>> handleConflict(UsernameAlreadyExist ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
            FileNotFoundException.class,
            FolderNotFoundException.class,
            UserNotFoundException.class,
            UsernameNotFoundException.class,
            FileTypeNotFoundException.class,
            NodeNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({IllegalArgumentException.class, DomainException.class, MismatchException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Error de validación en los argumentos");
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", errorMessage, request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large", "El archivo supera el tamaño máximo de carga permitido", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}
