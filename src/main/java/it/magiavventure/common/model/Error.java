package it.magiavventure.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    private String key;
    private Throwable throwable;
    private Object[] args;
}
