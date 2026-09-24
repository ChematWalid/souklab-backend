package com.project.souklab.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entity representing an artisan bookmarked by a client.
 */
@Entity
@Table(
    name = "client_favorite_artisans",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_client_favorite_artisans_client_artisan",
            columnNames = {"client_id", "artisan_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_client_favorite_artisans_client_created",
            columnList = "client_id, created_at DESC"
        ),
        @Index(
            name = "idx_client_favorite_artisans_artisan",
            columnList = "artisan_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientFavoriteArtisan extends ClientFavorite {

    /**
     * The artisan favorited by the client.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artisan_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Artisan artisan;

    /**
     * Explicit builder constructor allowing client and artisan initialization.
     *
     * @param client the owning client
     * @param artisan the favorited artisan
     */
    @Builder
    public ClientFavoriteArtisan(Client client, Artisan artisan) {
        super(client);
        this.artisan = artisan;
    }
}
