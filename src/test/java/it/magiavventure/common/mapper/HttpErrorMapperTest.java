package it.magiavventure.common.mapper;

import it.magiavventure.common.configuration.CommonProperties.ErrorsProperties.ErrorMessage;
import it.magiavventure.common.model.HttpError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Http error mapping tests")
class HttpErrorMapperTest {

    private final HttpErrorMapper httpErrorMapper = Mappers.getMapper(HttpErrorMapper.class);

    @Test
    @DisplayName("Map ErrorMessage in HttpError object")
    void mapErrorMessage_asHttpError_ok() {
        ErrorMessage errorMessage = ErrorMessage
                .builder()
                .code("code")
                .status(400)
                .description("description")
                .message("message")
                .build();

        HttpError httpError = httpErrorMapper.map(errorMessage);

        Assertions.assertNotNull(httpError);
        Assertions.assertEquals(errorMessage.getCode(), httpError.getCode());
        Assertions.assertEquals(errorMessage.getMessage(), httpError.getMessage());
        Assertions.assertEquals(errorMessage.getDescription(), httpError.getDescription());
        Assertions.assertEquals(errorMessage.getStatus(), httpError.getStatus());
    }

    @Test
    @DisplayName("Map ErrorMessage null in HttpError")
    void mapErrorMessage_asHttpError_null() {
        HttpError httpError = httpErrorMapper.map(null);

        Assertions.assertNull(httpError);
    }
}