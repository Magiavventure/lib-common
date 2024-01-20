package it.magiavventure.common.error.handler;

import it.magiavventure.common.configuration.CommonProperties;
import it.magiavventure.common.configuration.CommonProperties.ErrorsProperties.ErrorMessage;
import it.magiavventure.common.error.MagiavventureException;
import it.magiavventure.common.mapper.HttpErrorMapper;
import it.magiavventure.common.model.Error;
import it.magiavventure.common.model.HttpError;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@AllArgsConstructor
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {
    private final CommonProperties commonProperties;
    private final HttpErrorMapper httpErrorMapper;

    @ExceptionHandler({MagiavventureException.class})
    public ResponseEntity<HttpError> handleException(MagiavventureException magiavventureException) {

        Error error = magiavventureException.getError();
        ErrorMessage errorMessage = retrieveError(error.getKey());

        HttpError httpError = httpErrorMapper.map(errorMessage);
        httpError.setMessage(formatMessage(errorMessage.getMessage(), error.getArgs()));

        return ResponseEntity
                .status(httpError.getStatus())
                .body(httpError);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        ErrorMessage errorMessage = retrieveError(MagiavventureException.VALIDATION_ERROR);

        List<String> fields = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> fields.add(fieldError.getField()));
        HttpError httpError = httpErrorMapper.map(errorMessage);
        httpError.setFields(fields);

        return ResponseEntity
                .status(httpError.getStatus())
                .body(httpError);
    }

    private ErrorMessage retrieveError(@NotNull String key) {
        var errorMessage = commonProperties
                .getErrors()
                .retrieveErrorsMessages()
                .get(key);

        if(Objects.isNull(errorMessage)) return retrieveError(MagiavventureException.UNKNOWN_ERROR);


        return errorMessage;
    }

    private String formatMessage(String message, Object... args) {
        if(args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }

}
