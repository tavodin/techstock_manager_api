package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitRequestDTO;
import io.github.tavodin.techstock_manager.services.UnitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @PreAuthorize("hasAuthority('READ_UNIT')")
    @GetMapping("/{id}")
    public UnitDTO findById(@PathVariable Long id) {
        return unitService.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_UNIT')")
    @GetMapping
    public PagedModel<UnitDTO> findAll(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(name = "name", required = false) String name
    ) {
        return unitService.findAll(name, pageable);
    }

    @PreAuthorize("hasAuthority('CREATE_UNIT')")
    @PostMapping
    public ResponseEntity<UnitDTO> save(@RequestBody @Valid UnitRequestDTO responseDTO) {
        UnitDTO dto = unitService.save(responseDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PreAuthorize("hasAuthority('UPDATE_UNIT')")
    @PutMapping("/{id}")
    public UnitDTO update(@PathVariable Long id, @RequestBody @Valid UnitRequestDTO responseDTO) {
        return unitService.update(id, responseDTO);
    }

    @PreAuthorize("hasAuthority('DELETE_UNIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
