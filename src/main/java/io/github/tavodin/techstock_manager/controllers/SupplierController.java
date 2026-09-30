package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.SupplierAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.dto.SupplierRequestDTO;
import io.github.tavodin.techstock_manager.dto.purchase.PurchaseDTO;
import io.github.tavodin.techstock_manager.services.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_SUPPLIER')")
    @GetMapping("/{id}")
    public SupplierDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_SUPPLIER')")
    @GetMapping
    public PagedModel<SupplierDTO> findAll(
            @RequestParam(value = "name", required = false)
            String name,
            Pageable pageable
    ) {
        return service.findAll(name, pageable);
    }

    @PreAuthorize("hasAuthority('READ_SUPPLIER')")
    @GetMapping("/autocomplete")
    public List<SupplierAutocompleteDTO> getAllAutocomplete(@RequestParam("name") String name) {
        return service.getAllAutocomplete(name);
    }

    @PreAuthorize("hasAuthority('READ_SUPPLIER')")
    @GetMapping("/{id}/purchases")
    public PagedModel<PurchaseDTO> getAllPurchasesBySupplierId(
            @PathVariable Long id,
            Pageable pageable
    ) {
        return service.getAllPurchasesBySupplierId(id, pageable);
    }

    @PreAuthorize("hasAuthority('CREATE_SUPPLIER')")
    @PostMapping
    public ResponseEntity<SupplierDTO> save(@Valid @RequestBody SupplierRequestDTO request) {
        SupplierDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PreAuthorize("hasAuthority('UPDATE_SUPPLIER')")
    @PutMapping("/{id}")
    public SupplierDTO update(@PathVariable Long id, @Valid @RequestBody SupplierRequestDTO requestDTO) {
        return service.update(id, requestDTO);
    }

    @PreAuthorize("hasAuthority('DELETE_SUPPLIER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
