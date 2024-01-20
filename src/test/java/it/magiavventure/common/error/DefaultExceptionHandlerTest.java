package it.magiavventure.common.error;

import it.magiavventure.common.configuration.CommonProperties;
import it.magiavventure.common.configuration.CommonProperties.ErrorsProperties;
import it.magiavventure.common.configuration.CommonProperties.ErrorsProperties.ErrorMessage;
import it.magiavventure.common.error.handler.DefaultExceptionHandler;
import it.magiavventure.common.mapper.HttpErrorMapper;
import it.magiavventure.common.model.HttpError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;


@ExtendWith(MockitoExtension.class)
@DisplayName("Default exception handler tests")
class DefaultExceptionHandlerTest {


    @InjectMocks
    private DefaultExceptionHandler defaultExceptionHandler;

    @Spy
    private CommonProperties commonProperties = retrieveCommonProperties();

    @Spy
    private HttpErrorMapper httpErrorMapper = Mappers.getMapper(HttpErrorMapper.class);

    @ParameterizedTest
    @CsvSource({"unknown-error, unknown-error, errore sconosciuto, desc sconosciuta, 500, prova",
            "user-not-found, user-not-found, user non trovato, desc user non trovato, 404, prova",
            "user-exists, user-exists, il nome 'prova' non è disponibile, desc nome già esistente, 403, prova",
            "error-not-exists, unknown-error, errore sconosciuto, desc sconosciuta, 500, prova"})
    @DisplayName("Handle exception and return ResponseEntity")
    void handleExceptionTest(String code, String expectedCode, String expectedMessage,
                                 String expectedDescription, int expectedStatus,
                                 String args) {

        var magiavventureException = MagiavventureException.of(code, args);

        ResponseEntity<HttpError> responseEntity = defaultExceptionHandler.handleException(magiavventureException);

        Assertions.assertNotNull(responseEntity);
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(expectedStatus, responseEntity.getStatusCode().value());
        Assertions.assertEquals(expectedCode, responseEntity.getBody().getCode());
        Assertions.assertEquals(expectedMessage, responseEntity.getBody().getMessage());
        Assertions.assertEquals(expectedDescription, responseEntity.getBody().getDescription());
        Assertions.assertEquals(expectedStatus, responseEntity.getBody().getStatus());
    }

    @ParameterizedTest
    @CsvSource({"unknown-error, unknown-error, errore sconosciuto, desc sconosciuta, 500",
            "user-not-found, user-not-found, user non trovato, desc user non trovato, 404",
            "user-exists, user-exists, il nome '%s' non è disponibile, desc nome già esistente, 403",
            "error-not-exists, unknown-error, errore sconosciuto, desc sconosciuta, 500"})
    @DisplayName("Handle exception and return ResponseEntity without arguments")
    void handleExceptionTest(String code, String expectedCode, String expectedMessage,
                                 String expectedDescription, int expectedStatus) {

        var magiavventureException = MagiavventureException.of(code);

        ResponseEntity<HttpError> responseEntity = defaultExceptionHandler.handleException(magiavventureException);

        Assertions.assertNotNull(responseEntity);
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(expectedStatus, responseEntity.getStatusCode().value());
        Assertions.assertEquals(expectedCode, responseEntity.getBody().getCode());
        Assertions.assertEquals(expectedMessage, responseEntity.getBody().getMessage());
        Assertions.assertEquals(expectedDescription, responseEntity.getBody().getDescription());
        Assertions.assertEquals(expectedStatus, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("Handle MethodArgumentValidException and return ResponseEntity with fields in error")
    void handleMethodArgumentValidException() {
        MethodArgumentNotValidException exception = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("test", "field", "non null");
        Mockito.when(exception.getBindingResult())
                .thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<HttpError> responseEntity = defaultExceptionHandler.handleMethodArgumentNotValid(exception);

        Mockito.verify(bindingResult).getFieldErrors();
        Mockito.verify(exception).getBindingResult();

        Assertions.assertNotNull(responseEntity);
        HttpError error = responseEntity.getBody();
        Assertions.assertNotNull(error);
        Assertions.assertEquals("validation-error", error.getCode());
        Assertions.assertEquals(400, error.getStatus());
        Assertions.assertEquals("Si è verificato un errore", error.getMessage());
        Assertions.assertEquals("Dei campi non sono stati compilati correttamente", error.getDescription());
        Assertions.assertEquals(1, error.getFields().size());
    }

    @Test
    @DisplayName("Handle NotFoundException and return ResponseEntity")
    void handleNotFoundException() {
        ResponseEntity<HttpError> responseEntity = defaultExceptionHandler
                .handleNoHandlerException(new NoHandlerFoundException("GET", "/test", HttpHeaders.EMPTY));

        Assertions.assertNotNull(responseEntity);
        HttpError error = responseEntity.getBody();
        Assertions.assertNotNull(error);
        Assertions.assertEquals("not-found", error.getCode());
        Assertions.assertEquals(404, error.getStatus());
        Assertions.assertEquals("Si è verificato un errore", error.getMessage());
        Assertions.assertEquals("La risorsa non è stata trovata", error.getDescription());
    }

    @Test
    @DisplayName("Handle generic Exception and return ResponseEntity")
    void handleGenericException() {
        ResponseEntity<HttpError> responseEntity = defaultExceptionHandler
                .handleDefaultException(new Exception("errore nei test"));

        Assertions.assertNotNull(responseEntity);
        HttpError error = responseEntity.getBody();
        Assertions.assertNotNull(error);
        Assertions.assertEquals("service-unavailable", error.getCode());
        Assertions.assertEquals(503, error.getStatus());
        Assertions.assertEquals("Si è verificato un errore", error.getMessage());
        Assertions.assertEquals("Il servizio non è al momento disponibile", error.getDescription());
    }

    @Test
    @DisplayName("Handle client Exception and return ResponseEntity")
    void handleClientException() {
        ResponseEntity<HttpError> responseEntity = defaultExceptionHandler
                .handleClientException(new HttpRequestMethodNotSupportedException("errore nei test"));

        Assertions.assertNotNull(responseEntity);
        HttpError error = responseEntity.getBody();
        Assertions.assertNotNull(error);
        Assertions.assertEquals("bad-request", error.getCode());
        Assertions.assertEquals(400, error.getStatus());
        Assertions.assertEquals("Si è verificato un errore", error.getMessage());
        Assertions.assertEquals("È presente un errore nella richiesta", error.getDescription());
    }

    private CommonProperties retrieveCommonProperties() {
        var commonProperties = new CommonProperties();
        var errorProperties = new ErrorsProperties();
        var mapErrorMessages = new HashMap<String, ErrorMessage>();
        mapErrorMessages.put("unknown-error", ErrorMessage
                .builder()
                .code("unknown-error")
                .status(500)
                .message("errore sconosciuto")
                .description("desc sconosciuta")
                .build());
        mapErrorMessages.put("user-not-found", ErrorMessage
                .builder()
                .code("user-not-found")
                .status(404)
                .message("user non trovato")
                .description("desc user non trovato")
                .build());
        mapErrorMessages.put("user-exists", ErrorMessage
                .builder()
                .code("user-exists")
                .status(403)
                .message("il nome '%s' non è disponibile")
                .description("desc nome già esistente")
                .build());
        mapErrorMessages.put("validation-error", ErrorMessage
                .builder()
                .code("validation-error")
                .status(400)
                .message("Si è verificato un errore")
                .description("Dei campi non sono stati compilati correttamente")
                .build());
        mapErrorMessages.put("not-found", ErrorMessage
                .builder()
                .code("not-found")
                .status(404)
                .message("Si è verificato un errore")
                .description("La risorsa non è stata trovata")
                .build());
        mapErrorMessages.put("service-unavailable", ErrorMessage
                .builder()
                .code("service-unavailable")
                .status(503)
                .message("Si è verificato un errore")
                .description("Il servizio non è al momento disponibile")
                .build());
        mapErrorMessages.put("bad-request", ErrorMessage
                .builder()
                .code("bad-request")
                .status(400)
                .message("Si è verificato un errore")
                .description("È presente un errore nella richiesta")
                .build());
        errorProperties.setErrorsMessages(mapErrorMessages);
        commonProperties.setErrors(errorProperties);
        return commonProperties;
    }


}