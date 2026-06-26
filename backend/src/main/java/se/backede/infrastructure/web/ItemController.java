package se.backede.infrastructure.web;

import se.backede.application.dto.CreateItemRequest;
import se.backede.application.dto.ItemResponse;
import se.backede.application.dto.UpdateItemRequest;
import se.backede.application.usecase.ItemUseCaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemUseCaseService itemUseCaseService;

    public ItemController(ItemUseCaseService itemUseCaseService) {
        this.itemUseCaseService = itemUseCaseService;
    }

    @PostMapping
    ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemUseCaseService.create(request));
    }

    @GetMapping
    List<ItemResponse> list() {
        return itemUseCaseService.list();
    }

    @GetMapping("/{id}")
    ItemResponse getById(@PathVariable UUID id) {
        return itemUseCaseService.getById(id);
    }

    @PutMapping("/{id}")
    ItemResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request) {
        return itemUseCaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemUseCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
