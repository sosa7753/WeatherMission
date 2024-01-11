package zerobase.weather.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Client -> 서버 시점의 에러
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)

    // Controller 안의 에러
    @ExceptionHandler(Exception.class)
    public Exception handleAllException() {
        System.out.println("error from GlobalExceptionHandler");
        return new Exception();
    }
}
