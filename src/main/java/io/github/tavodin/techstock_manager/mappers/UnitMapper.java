package io.github.tavodin.techstock_manager.mappers;

import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitResponseDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    UnitDTO toModel(Unit unit);
    Unit toEntity(UnitResponseDTO dto);
}
