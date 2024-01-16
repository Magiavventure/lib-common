package it.magiavventure.common.error.handler;

import it.magiavventure.common.configuration.CommonProperties;
import it.magiavventure.common.configuration.CommonProperties.ErrorsProperties.ErrorMessage;
import it.magiavventure.common.error.MagiavventureException;
import it.magiavventure.common.mapper.HttpErrorMapper;
import it.magiavventure.common.model.Error;
import it.magiavventure.common.model.HttpError;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
@AllArgsConstructor
public class DefaultExceptionHandler {
    private final CommonProperties commonProperties;
    private final HttpErrorMapper httpErrorMapper;

    @ExceptionHandler({MagiavventureException.class})
    public ResponseEntity<HttpError> exceptionHandler(MagiavventureException magiavventureException) {

        Error error = magiavventureException.getError();
        ErrorMessage errorMessage = retrieveError(error.getKey());

        HttpError httpError = httpErrorMapper.map(errorMessage);
        httpError.setMessage(formatMessage(errorMessage.getMessage(), error.getArgs()));

        return ResponseEntity
                .status(httpError.getStatus())
                .body(httpError);
    }

    private ErrorMessage retrieveError(@NotNull String key) {
        var errorMessage = commonProperties
                .getErrors()
                .getErrorsMessages()
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
