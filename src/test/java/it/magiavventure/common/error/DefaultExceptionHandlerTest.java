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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

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
    void handleMethodArgumentValidException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MethodArgumentNotValidException exception = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("test", "field", "non null");
        Mockito.when(exception.getBindingResult())
                .thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<Object> responseEntity = defaultExceptionHandler.handleException(exception,
                new ServletWebRequest(request));

        Mockito.verify(bindingResult).getFieldErrors();
        Mockito.verify(exception).getBindingResult();

        Assertions.assertNotNull(responseEntity);
        HttpError error = (HttpError) responseEntity.getBody();
        Assertions.assertNotNull(error);
        Assertions.assertEquals("validation-error", error.getCode());
        Assertions.assertEquals("Si è verificato un errore", error.getMessage());
        Assertions.assertEquals("Dei campi non sono stati compilati correttamente", error.getDescription());
        Assertions.assertEquals(1, error.getFields().size());

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
        errorProperties.setErrorsMessages(mapErrorMessages);
        commonProperties.setErrors(errorProperties);
        return commonProperties;
    }


}