package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationRequestDTO;
import io.github.tavodin.techstock_manager.services.SpecificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/specifications")
public class SpecificationController {

    private final SpecificationService service;

    public SpecificationController(SpecificationService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_SPECIFICATION')")
    @GetMapping("/{id}")
    public SpecificationDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_SPECIFICATION')")
    @GetMapping
    public PagedModel<SpecificationDTO> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PreAuthorize("hasAuthority('CREATE_SPECIFICATION')")
    @PostMapping
    public ResponseEntity<SpecificationDTO> save(@RequestBody @Valid SpecificationRequestDTO request) {
        SpecificationDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PreAuthorize("hasAuthority('UPDATE_SPECIFICATION')")
    @PutMapping("/{id}")
    public SpecificationDTO update(@PathVariable Long id, @RequestBody @Valid SpecificationRequestDTO request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasAuthority('DELETE_SPECIFICATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
