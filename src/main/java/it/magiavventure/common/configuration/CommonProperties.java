package it.magiavventure.common.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "magiavventure.lib.common")
public class CommonProperties {

    private ErrorsProperties errors;

    @Data
    @NoArgsConstructor
    public static class ErrorsProperties {
        private Map<String, ErrorMessage> errorsMessages = Collections.emptyMap();

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
    }
}
