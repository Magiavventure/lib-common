package it.magiavventure.common.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@ConfigurationProperties(prefix = "magiavventure.lib.common")
public class CommonProperties {

    private ErrorsProperties errors;

    @Data
    @NoArgsConstructor
    public static class ErrorsProperties {
        private Map<String, ErrorMessage> errorsMessages = Collections.emptyMap();
        private Map<String, ErrorMessage> serviceErrorsMessages = Collections.emptyMap();

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ErrorMessage {
            private String message;
            private String description;
            private String code;
            private int status;
        }

        private BinaryOperator<ErrorMessage> mergeErrorMessage =
                (defaultErrorMessage, serviceErrorMessage) -> serviceErrorMessage;


        public Map<String, ErrorMessage> retrieveErrorsMessages() {
            return Stream.concat(errorsMessages.entrySet().stream(), serviceErrorsMessages.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeErrorMessage));
        }
    }
}
