package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitResponseDTO;
import io.github.tavodin.techstock_manager.services.UnitService;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UnitDTO> save(@RequestBody UnitResponseDTO responseDTO) {
        return null;
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
