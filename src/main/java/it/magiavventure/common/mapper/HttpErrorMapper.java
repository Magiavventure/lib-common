package it.magiavventure.common.mapper;

import it.magiavventure.common.configuration.CommonProperties.ErrorsProperties.ErrorMessage;
import it.magiavventure.common.model.HttpError;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HttpErrorMapper {
    HttpError map(ErrorMessage errorMessage);
}
