package org.example.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.example.dto.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        return ResponseEntity.badRequest().body(
                ResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(errorMessages)
                        .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleEnumParseError(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException &&
                ((InvalidFormatException) ex.getCause()).getTargetType().isEnum()) {
            return ResponseEntity.badRequest().body("Invalid value for enum: " + ex.getMessage());
        }
        return ResponseEntity.badRequest().body("Invalid request");
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleDataNotFound(DataNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseDTO.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ResponseDTO> handleResourceExists(ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseDTO.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler({
            InvalidCredentialException.class,
            InvalidPasswordException.class,
            InvalidRefreshToken.class,
            InvalidFileException.class,
            InvalidDraftPackageException.class,
            InvalidStatusTransitionException.class,
            InvalidProductUpdated.class
    })
    public ResponseEntity<ResponseDTO> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler({
            UnauthorizedAccessException.class,
            UserInactiveException.class
    })
    public ResponseEntity<ResponseDTO> handleUnauthorized(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseDTO.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ImageLimitExceededException.class)
    public ResponseEntity<ResponseDTO> handleImageLimit(ImageLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public ResponseEntity<ResponseDTO> handleOutOfStock(NotEnoughStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseDTO.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Internal server error")
                        .build()
        );
    }
}
