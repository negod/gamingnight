package se.backede.infrastructure.persistence;

import se.backede.domain.model.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@Import({JpaItemRepositoryAdapter.class, ItemJpaMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaItemRepositoryAdapterTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private JpaItemRepositoryAdapter adapter;

    @Test
    void savesAndFindsItem() {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        var item = Item.create("Desk", "Standing desk", now);

        adapter.save(item);

        assertThat(adapter.findById(item.id()))
                .isPresent()
                .get()
                .extracting(Item::title)
                .isEqualTo("Desk");
    }

    @Test
    void returnsEmptyForUnknownItem() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void deletesItem() {
        var item = adapter.save(Item.create("Desk", "", Instant.now()));

        adapter.deleteById(item.id());

        assertThat(adapter.existsById(item.id())).isFalse();
    }
}
