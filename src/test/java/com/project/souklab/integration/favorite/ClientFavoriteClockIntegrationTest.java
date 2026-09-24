package com.project.souklab.integration.favorite;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.ClientFavoriteArtisanRepository;
import com.project.souklab.dao.ClientRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanItemDTO;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanResponseDTO;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.Client;
import com.project.souklab.model.ClientFavoriteArtisan;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import com.project.souklab.service.favorite.ArtisanFavoriteService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Live MariaDB integration tests verifying that the application's injected {@link Clock} bean
 * accurately drives artisan visibility decisions during favorite addition, and that the
 * persisted timestamp equals the response and list representations.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.search.enabled=false"
})
class ClientFavoriteClockIntegrationTest {

    @MockitoBean
    private Clock clock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ArtisanRepository artisanRepository;

    @Autowired
    private ClientFavoriteArtisanRepository favoriteRepository;

    @Autowired
    private ArtisanFavoriteService artisanFavoriteService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    private final List<String> createdFavoriteIds = new ArrayList<>();
    private final List<String> createdClientIds = new ArrayList<>();
    private final List<String> createdArtisanIds = new ArrayList<>();
    private final List<String> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUpClock() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-06-01T10:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        transactionTemplate.execute(status -> {
            for (String favId : createdFavoriteIds) {
                entityManager.createNativeQuery("DELETE FROM client_favorite_artisans WHERE id = :id")
                        .setParameter("id", favId)
                        .executeUpdate();
            }
            for (String clientId : createdClientIds) {
                entityManager.createNativeQuery("DELETE FROM clients WHERE id = :id")
                        .setParameter("id", clientId)
                        .executeUpdate();
            }
            for (String artisanId : createdArtisanIds) {
                entityManager.createNativeQuery("DELETE FROM artisans WHERE id = :id")
                        .setParameter("id", artisanId)
                        .executeUpdate();
            }
            for (String userId : createdUserIds) {
                entityManager.createNativeQuery("DELETE FROM users WHERE id = :id")
                        .setParameter("id", userId)
                        .executeUpdate();
            }
            return null;
        });
    }

    private User createTestUser(String emailPrefix, AccountStatus status) {
        return transactionTemplate.execute(statusTx -> {
            String id = UUID.randomUUID().toString();
            User user = User.builder()
                    .email(emailPrefix + "-" + UUID.randomUUID() + "@souklab.dz")
                    .firstName("ClockTest")
                    .lastName("User")
                    .status(status)
                    .emailVerified(true)
                    .build();
            user.setId(id);
            entityManager.persist(user);
            entityManager.flush();
            createdUserIds.add(user.getId());
            return user;
        });
    }

    private Client createTestClient(User user) {
        return transactionTemplate.execute(statusTx -> {
            User managedUser = entityManager.find(User.class, user.getId());
            Client client = Client.builder()
                    .user(managedUser)
                    .build();
            entityManager.persist(client);
            managedUser.setClient(client);
            entityManager.flush();
            createdClientIds.add(client.getId());
            return client;
        });
    }

    private Artisan createTestArtisan(User user) {
        return transactionTemplate.execute(statusTx -> {
            User managedUser = entityManager.find(User.class, user.getId());
            Artisan artisan = Artisan.builder()
                    .id(managedUser.getId())
                    .user(managedUser)
                    .bio("Artisan bio")
                    .city("Algiers")
                    .isVerified(true)
                    .rating(4.5)
                    .reviewsCount(5)
                    .viewsCount(100)
                    .build();
            entityManager.persist(artisan);
            managedUser.setArtisan(artisan);
            entityManager.flush();
            createdArtisanIds.add(artisan.getId());
            return artisan;
        });
    }

    /**
     * Verifies that the injected Clock drives the visibility decision for suspended artisans,
     * and that the favoritedAt timestamp matches across POST response, database, and GET list representation.
     */
    @Test
    @DisplayName("Clock-driven visibility: suspended artisan rejected before ban expires, accepted after clock advances")
    void clockDrivesSuspendedArtisanVisibilityAndTimestampsMatchAcrossLayers() {
        User clientUser = createTestUser("client-clock", AccountStatus.ACTIVE);
        Client client = createTestClient(clientUser);

        LocalDateTime banUntil = LocalDateTime.of(2026, 6, 1, 12, 0, 0);
        User artisanUser = createTestUser("artisan-clock", AccountStatus.SUSPENDED);
        transactionTemplate.execute(status -> {
            User managedArtisanUser = entityManager.find(User.class, artisanUser.getId());
            managedArtisanUser.setBannedUntil(banUntil);
            entityManager.flush();
            return null;
        });
        Artisan artisan = createTestArtisan(artisanUser);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                clientUser.getEmail(), "N/A", List.of(Permission.Client.FAVORITES)));
        SecurityContextHolder.setContext(context);

        /**
         * 1. Clock is set to 2 hours before ban expiration.
         * The artisan is not effectively visible, so addFavorite must throw ResourceNotFoundException.
         */
        Instant beforeBanExpiry = Instant.parse("2026-06-01T10:00:00Z");
        when(clock.instant()).thenReturn(beforeBanExpiry);

        assertThatThrownBy(() -> artisanFavoriteService.addFavorite(artisan.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artisan not found with id: " + artisan.getId());

        /**
         * 2. Clock advances to 2 hours after ban expiration.
         * The suspension has lapsed, so the artisan is effectively visible and addFavorite must succeed.
         */
        Instant afterBanExpiry = Instant.parse("2026-06-01T14:00:00Z");
        when(clock.instant()).thenReturn(afterBanExpiry);

        ClientFavoriteArtisanResponseDTO response = artisanFavoriteService.addFavorite(artisan.getId());
        createdFavoriteIds.add(response.getFavoriteId());

        assertThat(response).isNotNull();
        assertThat(response.getArtisanId()).isEqualTo(artisan.getId());
        assertThat(response.getFavoritedAt()).isNotNull();

        /**
         * 3. Assert favoritedAt in POST response equals created_at in the database.
         */
        ClientFavoriteArtisan persistedFavorite = transactionTemplate.execute(status -> {
            ClientFavoriteArtisan fav = entityManager.find(ClientFavoriteArtisan.class, response.getFavoriteId());
            assertThat(fav).isNotNull();
            return fav;
        });

        assertThat(response.getFavoritedAt()).isEqualTo(persistedFavorite.getCreatedAt());

        /**
         * 4. Assert favoritedAt in listFavorites equals POST response and database record.
         */
        PaginatedResponse<ClientFavoriteArtisanItemDTO> listResponse =
                artisanFavoriteService.listFavorites(PageRequest.of(0, 10));

        assertThat(listResponse.getContent()).hasSize(1);
        ClientFavoriteArtisanItemDTO listedItem = listResponse.getContent().getFirst();
        assertThat(listedItem.getFavoritedAt()).isEqualTo(response.getFavoritedAt());
        assertThat(listedItem.getFavoritedAt()).isEqualTo(persistedFavorite.getCreatedAt());
    }
}
