package se.backede.application.mapper;

import se.backede.application.dto.ItemResponse;
import se.backede.domain.model.Item;

public final class ItemDtoMapper {

    private ItemDtoMapper() {
    }

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.id(),
                item.title(),
                item.description(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
