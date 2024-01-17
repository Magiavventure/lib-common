package it.magiavventure.common.error;

import it.magiavventure.common.model.Error;
import lombok.Getter;

@Getter
public class MagiavventureException extends RuntimeException {

    public static final String UNKNOWN_ERROR = "unknown-error";
    public static final String JWT_EXPIRED = "jwt-expired";
    public static final String JWT_NOT_VALID = "jwt-not-valid";

    private final transient Error error;

    public MagiavventureException(Error error) {
        super(error.getKey(), error.getThrowable());
        this.error = error;
    }

    public static MagiavventureException of(String key, String... args) {
        final var error = Error
                .builder()
                .key(key)
                .args(args)
                .build();
        return new MagiavventureException(error);
    }

}
