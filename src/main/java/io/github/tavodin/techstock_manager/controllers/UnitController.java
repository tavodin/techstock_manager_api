package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitResponseDTO;
import io.github.tavodin.techstock_manager.services.UnitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/unit")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping("/{id}")
    public UnitDTO findById(@PathVariable Long id) {
        return unitService.findById(id);
    }

    @GetMapping
    public PagedModel<UnitDTO> findAll(Pageable pageable) {
        return unitService.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<UnitDTO> save(@RequestBody @Valid UnitResponseDTO responseDTO) {
        UnitDTO dto = unitService.save(responseDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitDTO> update(@PathVariable Long id, @RequestBody UnitResponseDTO responseDTO) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return null;
    }
}
