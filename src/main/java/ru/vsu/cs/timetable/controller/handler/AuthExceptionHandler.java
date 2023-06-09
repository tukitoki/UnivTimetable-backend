package ru.vsu.cs.timetable.controller.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.vsu.cs.timetable.exception.AuthException;
import ru.vsu.cs.timetable.exception.message.ErrorMessage;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorMessage> handleAuthException(AuthException ex) {
        AuthException.CODE code = ex.getCode();
        HttpStatus status = switch (code) {
            case JWT_VALIDATION_ERROR -> BAD_REQUEST;
        };

        String codeStr = code.toString();

        log.error(codeStr, ex);

        return ResponseEntity
                .status(status)
                .body(new ErrorMessage(codeStr, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthException(AuthenticationException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorMessage(BAD_REQUEST.name(), ex.getMessage()));
    }
}
