package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.Client;
import com.project.souklab.model.ClientFavoriteArtisan;
import com.project.souklab.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test verifying query derivation, visibility filtering, pagination,
 * and lock retrieval for {@link ClientFavoriteArtisanRepository} and {@link ClientRepository}.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ClientFavoriteArtisanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientFavoriteArtisanRepository favoriteRepository;

    @Autowired
    private ClientRepository clientRepository;

    private final LocalDateTime referenceTime = LocalDateTime.of(2026, 9, 23, 12, 0, 0);

    private Client createClient(String email) {
        User user = User.builder()
                .email(email)
                .firstName("Client")
                .lastName("User")
                .status(AccountStatus.ACTIVE)
                .build();
        entityManager.persist(user);

        Client client = Client.builder()
                .user(user)
                .build();
        return entityManager.persist(client);
    }

    private Artisan createArtisan(String email, AccountStatus status, LocalDateTime bannedUntil, boolean softDeleted) {
        User user = User.builder()
                .email(email)
                .firstName("Artisan")
                .lastName("Craft")
                .status(status)
                .bannedUntil(bannedUntil)
                .build();
        entityManager.persist(user);

        Artisan artisan = Artisan.builder()
                .user(user)
                .deletedAt(softDeleted ? referenceTime.minusDays(1) : null)
                .build();
        return entityManager.persist(artisan);
    }

    @Test
    @DisplayName("exists and findByClientIdAndArtisanId detect favorite records correctly")
    void detectsExistingAndMissingFavorites() {
        Client client = createClient("c1@test.com");
        Artisan artisan = createArtisan("a1@test.com", AccountStatus.ACTIVE, null, false);

        assertThat(favoriteRepository.existsByClientIdAndArtisanId(client.getId(), artisan.getId())).isFalse();
        assertThat(favoriteRepository.findByClientIdAndArtisanId(client.getId(), artisan.getId())).isEmpty();

        ClientFavoriteArtisan favorite = ClientFavoriteArtisan.builder()
                .client(client)
                .artisan(artisan)
                .build();
        entityManager.persist(favorite);
        entityManager.flush();
        entityManager.clear();

        assertThat(favoriteRepository.existsByClientIdAndArtisanId(client.getId(), artisan.getId())).isTrue();
        Optional<ClientFavoriteArtisan> found = favoriteRepository.findByClientIdAndArtisanId(client.getId(), artisan.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getClient().getId()).isEqualTo(client.getId());
        assertThat(found.get().getArtisan().getId()).isEqualTo(artisan.getId());
    }

    @Test
    @DisplayName("countByClientId counts favorites accurately per client")
    void countsFavoritesPerClient() {
        Client client1 = createClient("c2@test.com");
        Client client2 = createClient("c3@test.com");
        Artisan artisan1 = createArtisan("a2@test.com", AccountStatus.ACTIVE, null, false);
        Artisan artisan2 = createArtisan("a3@test.com", AccountStatus.ACTIVE, null, false);

        entityManager.persist(ClientFavoriteArtisan.builder().client(client1).artisan(artisan1).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client1).artisan(artisan2).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client2).artisan(artisan1).build());
        entityManager.flush();
        entityManager.clear();

        assertThat(favoriteRepository.countByClientId(client1.getId())).isEqualTo(2);
        assertThat(favoriteRepository.countByClientId(client2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("findVisibleByClientId filters non-visible artisans and honors pagination")
    void filtersNonVisibleArtisansAndPaginates() {
        Client client = createClient("c4@test.com");

        Artisan visible1 = createArtisan("v1@test.com", AccountStatus.ACTIVE, null, false);
        Artisan visible2 = createArtisan("v2@test.com", AccountStatus.ACTIVE, null, false);
        Artisan softDeleted = createArtisan("deleted@test.com", AccountStatus.ACTIVE, null, true);
        Artisan suspendedFuture = createArtisan("suspended-future@test.com", AccountStatus.SUSPENDED, referenceTime.plusDays(1), false);
        Artisan suspendedExpired = createArtisan("suspended-expired@test.com", AccountStatus.SUSPENDED, referenceTime.minusMinutes(10), false);
        Artisan suspendedNullBan = createArtisan("suspended-null@test.com", AccountStatus.SUSPENDED, null, false);

        entityManager.persist(ClientFavoriteArtisan.builder().client(client).artisan(visible1).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client).artisan(visible2).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client).artisan(softDeleted).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client).artisan(suspendedFuture).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client).artisan(suspendedExpired).build());
        entityManager.persist(ClientFavoriteArtisan.builder().client(client).artisan(suspendedNullBan).build());
        entityManager.flush();
        entityManager.clear();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ClientFavoriteArtisan> page = favoriteRepository.findVisibleByClientId(
                client.getId(),
                AccountStatus.SUSPENDED,
                referenceTime,
                pageRequest
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(fav -> fav.getArtisan().getId())
                .containsExactlyInAnyOrder(visible1.getId(), visible2.getId(), suspendedExpired.getId());
    }

    @Test
    @DisplayName("ClientRepository findWithLockById loads client entity successfully")
    void loadsClientWithLock() {
        Client client = createClient("c5@test.com");
        entityManager.flush();
        entityManager.clear();

        Optional<Client> locked = clientRepository.findWithLockById(client.getId());
        assertThat(locked).isPresent();
        assertThat(locked.get().getId()).isEqualTo(client.getId());
    }

    @Test
    @DisplayName("findVisibleByClientId with bogus sort property throws InvalidDataAccessApiUsageException")
    void findVisibleByClientId_bogusSort_throwsException() {
        Client client = createClient("c_bogus_sort@test.com");
        entityManager.flush();
        entityManager.clear();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bogusField"));

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
                favoriteRepository.findVisibleByClientId(
                        client.getId(),
                        AccountStatus.SUSPENDED,
                        referenceTime,
                        pageRequest
                )
        );

        assertThat(thrown).isNotNull();
        System.out.println("EXCEPTION_CHAIN_START");
        Throwable current = thrown;
        int level = 0;
        while (current != null) {
            System.out.println("Level " + level + ": " + current.getClass().getName() + " -> " + current.getMessage());
            current = current.getCause();
            level++;
        }
        System.out.println("EXCEPTION_CHAIN_END");

        assertThat(thrown).isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class);
    }
}
