package se.backede.infrastructure.persistence;

import se.backede.domain.model.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemJpaMapper {

    ItemEntity toEntity(Item item) {
        return new ItemEntity(
                item.id(),
                item.title(),
                item.description(),
                item.createdAt(),
                item.updatedAt()
        );
    }

    Item toDomain(ItemEntity entity) {
        return Item.rehydrate(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
