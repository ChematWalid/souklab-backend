package com.project.souklab.controller.favorite;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.controller.support.SecurityTestUtils;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanItemDTO;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanResponseDTO;
import com.project.souklab.dto.favorite.FavoriteStatusResponseDTO;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.service.favorite.ArtisanFavoriteService;
import com.project.souklab.security.Permission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test for {@link ClientFavoriteArtisanController}.
 * Verifies security authorization, response envelopes, error handling, and pagination clamping.
 */
@ControllerSliceTest(controllers = ClientFavoriteArtisanController.class)
class ClientFavoriteArtisanControllerTest {

    private static final String BASE_URL = "/api/v1/client/favorites/artisans";
    private static final String ARTISAN_ID = "artisan-uuid-123";
    private static final String FAVORITE_ID = "fav-uuid-456";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtisanFavoriteService artisanFavoriteService;

    @Nested
    @DisplayName("Authentication & Authorization")
    class SecurityTests {

        @Test
        @DisplayName("POST /artisans/{artisanId}: returns 401 when anonymous")
        void addFavorite_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("GET /artisans: returns 401 when anonymous")
        void listFavorites_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("GET /artisans/{artisanId}/status: returns 401 when anonymous")
        void isFavorited_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + ARTISAN_ID + "/status"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("DELETE /artisans/{artisanId}: returns 401 when anonymous")
        void removeFavorite_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + ARTISAN_ID))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("POST /artisans/{artisanId}: returns 403 when authenticated as Artisan")
        void addFavorite_artisanPrincipal_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.artisan()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("GET /artisans: returns 403 when authenticated as Artisan")
        void listFavorites_artisanPrincipal_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL).with(SecurityTestUtils.artisan()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("DELETE /artisans/{artisanId}: returns 403 when authenticated as Artisan")
        void removeFavorite_artisanPrincipal_returns403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.artisan()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("GET /artisans/{artisanId}/status: returns 403 when authenticated as Artisan")
        void isFavorited_artisanPrincipal_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + ARTISAN_ID + "/status").with(SecurityTestUtils.artisan()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("POST /artisans/{artisanId}: returns 403 when client token lacks favorites permission")
        void addFavorite_clientWithoutFavoritesPermission_returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(clientWithoutFavorites()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("GET /artisans: returns 403 when client token lacks favorites permission")
        void listFavorites_clientWithoutFavoritesPermission_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL).with(clientWithoutFavorites()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("GET /artisans/{artisanId}/status: returns 403 when client token lacks favorites permission")
        void isFavorited_clientWithoutFavoritesPermission_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + ARTISAN_ID + "/status").with(clientWithoutFavorites()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("DELETE /artisans/{artisanId}: returns 403 when client token lacks favorites permission")
        void removeFavorite_clientWithoutFavoritesPermission_returns403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + ARTISAN_ID).with(clientWithoutFavorites()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

            verifyNoInteractions(artisanFavoriteService);
        }

        @Test
        @DisplayName("POST /artisans/{artisanId}: returns 403 when admin lacks client profile in service layer")
        void addFavorite_adminWithoutClientProfile_returns403() throws Exception {
            when(artisanFavoriteService.addFavorite(ARTISAN_ID))
                    .thenThrow(new ForbiddenException("Only registered clients can manage favorites."));

            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.admin()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Only registered clients can manage favorites."));
        }

        @Test
        @DisplayName("GET /artisans: returns 403 when admin lacks client profile in service layer")
        void listFavorites_adminWithoutClientProfile_returns403() throws Exception {
            when(artisanFavoriteService.listFavorites(any(Pageable.class)))
                    .thenThrow(new ForbiddenException("Only registered clients can manage favorites."));

            mockMvc.perform(get(BASE_URL).with(SecurityTestUtils.admin()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Only registered clients can manage favorites."));
        }

        @Test
        @DisplayName("GET /artisans/{artisanId}/status: returns 403 when admin lacks client profile in service layer")
        void isFavorited_adminWithoutClientProfile_returns403() throws Exception {
            when(artisanFavoriteService.isFavorited(ARTISAN_ID))
                    .thenThrow(new ForbiddenException("Only registered clients can manage favorites."));

            mockMvc.perform(get(BASE_URL + "/" + ARTISAN_ID + "/status").with(SecurityTestUtils.admin()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Only registered clients can manage favorites."));
        }

        @Test
        @DisplayName("DELETE /artisans/{artisanId}: returns 403 when admin lacks client profile in service layer")
        void removeFavorite_adminWithoutClientProfile_returns403() throws Exception {
            doThrow(new ForbiddenException("Only registered clients can manage favorites."))
                    .when(artisanFavoriteService).removeFavorite(ARTISAN_ID);

            mockMvc.perform(delete(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.admin()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Only registered clients can manage favorites."));
        }

        private static RequestPostProcessor clientWithoutFavorites() {
            return user("client-noperm@souklab.com")
                    .authorities(Permission.Profile.READ);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/client/favorites/artisans/{artisanId}")
    class AddFavoriteTests {

        @Test
        @DisplayName("returns 201 Created with favorite metadata on success")
        void addFavorite_success_returns201() throws Exception {
            ClientFavoriteArtisanResponseDTO responseDTO = ClientFavoriteArtisanResponseDTO.builder()
                    .favoriteId(FAVORITE_ID)
                    .artisanId(ARTISAN_ID)
                    .favoritedAt(LocalDateTime.of(2026, 9, 23, 10, 15, 0))
                    .build();

            when(artisanFavoriteService.addFavorite(ARTISAN_ID)).thenReturn(responseDTO);

            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.client()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.data.favoriteId").value(FAVORITE_ID))
                    .andExpect(jsonPath("$.data.artisanId").value(ARTISAN_ID))
                    .andExpect(jsonPath("$.data.favoritedAt").value("2026-09-23T10:15:00"));

            verify(artisanFavoriteService).addFavorite(ARTISAN_ID);
        }

        @Test
        @DisplayName("returns 404 Not Found when artisan does not exist or is invisible")
        void addFavorite_artisanNotFound_returns404() throws Exception {
            when(artisanFavoriteService.addFavorite(ARTISAN_ID))
                    .thenThrow(new ResourceNotFoundException("Artisan not found with id: " + ARTISAN_ID));

            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.client()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Artisan not found with id: " + ARTISAN_ID));
        }

        @Test
        @DisplayName("returns 409 Conflict when artisan is already favorited")
        void addFavorite_duplicate_returns409() throws Exception {
            when(artisanFavoriteService.addFavorite(ARTISAN_ID))
                    .thenThrow(new ConflictException("Artisan is already favorited."));

            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.client()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("CONFLICT"))
                    .andExpect(jsonPath("$.message").value("Artisan is already favorited."));
        }

        @Test
        @DisplayName("returns 409 Conflict when client favorites cap is reached")
        void addFavorite_capReached_returns409() throws Exception {
            when(artisanFavoriteService.addFavorite(ARTISAN_ID))
                    .thenThrow(new ConflictException("Client favorite limit reached."));

            mockMvc.perform(post(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.client()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("CONFLICT"))
                    .andExpect(jsonPath("$.message").value("Client favorite limit reached."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/client/favorites/artisans")
    class ListFavoritesTests {

        @Test
        @DisplayName("returns 200 OK with paginated favorite items mapped to directory card shape")
        void listFavorites_success_returns200() throws Exception {
            ArtisanDirectoryCardDTO cardDTO = ArtisanDirectoryCardDTO.builder()
                    .id(ARTISAN_ID)
                    .artisanName("Karim Benali")
                    .city("Algiers")
                    .rating(4.8)
                    .reviewsCount(12)
                    .viewsCount(150)
                    .verified(true)
                    .build();

            ClientFavoriteArtisanItemDTO itemDTO = ClientFavoriteArtisanItemDTO.builder()
                    .favoritedAt(LocalDateTime.of(2026, 9, 23, 10, 15, 0))
                    .artisan(cardDTO)
                    .build();

            PaginatedResponse<ClientFavoriteArtisanItemDTO> pageResponse = PaginatedResponse.<ClientFavoriteArtisanItemDTO>builder()
                    .content(List.of(itemDTO))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(artisanFavoriteService.listFavorites(any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get(BASE_URL).with(SecurityTestUtils.client()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].favoritedAt").value("2026-09-23T10:15:00"))
                    .andExpect(jsonPath("$.data.content[0].artisan.id").value(ARTISAN_ID))
                    .andExpect(jsonPath("$.data.content[0].artisan.artisanName").value("Karim Benali"));
        }

        @Test
        @DisplayName("clamps requested page size above 100 to global maximum of 100 (e.g. size=99999)")
        void listFavorites_pageSizeClamping_size99999ClampedTo100() throws Exception {
            PaginatedResponse<ClientFavoriteArtisanItemDTO> emptyResponse = PaginatedResponse.<ClientFavoriteArtisanItemDTO>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(100)
                    .totalElements(0L)
                    .totalPages(0)
                    .last(true)
                    .build();

            when(artisanFavoriteService.listFavorites(any(Pageable.class))).thenReturn(emptyResponse);

            mockMvc.perform(get(BASE_URL + "?size=99999").with(SecurityTestUtils.client()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(artisanFavoriteService).listFavorites(captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("passes custom page, size, and sort parameters transparently to service layer")
        void listFavorites_customPageAndSort_passedToService() throws Exception {
            PaginatedResponse<ClientFavoriteArtisanItemDTO> emptyResponse = PaginatedResponse.<ClientFavoriteArtisanItemDTO>builder()
                    .content(List.of())
                    .pageNumber(1)
                    .pageSize(10)
                    .totalElements(0L)
                    .totalPages(0)
                    .last(true)
                    .build();

            when(artisanFavoriteService.listFavorites(any(Pageable.class))).thenReturn(emptyResponse);

            mockMvc.perform(get(BASE_URL + "?page=1&size=10&sort=createdAt,asc").with(SecurityTestUtils.client()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(artisanFavoriteService).listFavorites(captor.capture());
            assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
            assertThat(captor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/client/favorites/artisans/{artisanId}/status")
    class CheckStatusTests {

        @Test
        @DisplayName("returns 200 OK with favorited=true when artisan is favorited")
        void isFavorited_true_returns200() throws Exception {
            when(artisanFavoriteService.isFavorited(ARTISAN_ID)).thenReturn(FavoriteStatusResponseDTO.of(true));

            mockMvc.perform(get(BASE_URL + "/" + ARTISAN_ID + "/status").with(SecurityTestUtils.client()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.favorited").value(true));
        }

        @Test
        @DisplayName("returns 200 OK with favorited=false when artisan is not favorited")
        void isFavorited_false_returns200() throws Exception {
            when(artisanFavoriteService.isFavorited(ARTISAN_ID)).thenReturn(FavoriteStatusResponseDTO.of(false));

            mockMvc.perform(get(BASE_URL + "/" + ARTISAN_ID + "/status").with(SecurityTestUtils.client()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.favorited").value(false));
        }

        @Test
        @DisplayName("returns 404 Not Found when artisan does not exist in DB")
        void isFavorited_artisanNotFound_returns404() throws Exception {
            when(artisanFavoriteService.isFavorited("missing-id"))
                    .thenThrow(new ResourceNotFoundException("Artisan not found with id: missing-id"));

            mockMvc.perform(get(BASE_URL + "/missing-id/status").with(SecurityTestUtils.client()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/client/favorites/artisans/{artisanId}")
    class RemoveFavoriteTests {

        @Test
        @DisplayName("returns 200 OK with data=null when favorite is removed")
        void removeFavorite_success_returns200() throws Exception {
            doNothing().when(artisanFavoriteService).removeFavorite(ARTISAN_ID);

            mockMvc.perform(delete(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.client()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(nullValue()));

            verify(artisanFavoriteService).removeFavorite(ARTISAN_ID);
        }

        @Test
        @DisplayName("returns 404 Not Found when favorite does not exist")
        void removeFavorite_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Favorite not found for artisan: " + ARTISAN_ID))
                    .when(artisanFavoriteService).removeFavorite(ARTISAN_ID);

            mockMvc.perform(delete(BASE_URL + "/" + ARTISAN_ID).with(SecurityTestUtils.client()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
        }
    }
}
