package com.project.souklab.controller.directory;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.controller.support.SecurityTestUtils;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import com.project.souklab.dto.directory.DirectorySearchFilterDTO;
import com.project.souklab.dto.directory.DirectorySortOrder;
import com.project.souklab.service.directory.DirectorySearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test verifying the public directory endpoint GET /api/v1/public/directory.
 * Validates parameter binding, pagination defaults, public unauthenticated access,
 * and Jakarta Bean Validation constraints on query parameters.
 */
@ControllerSliceTest(controllers = DirectoryController.class)
class DirectoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectorySearchService directorySearchService;

    /**
     * Verifies that anonymous (unauthenticated) calls are rejected by @PreAuthorize.
     *
     * <p>The directory URL is under {@code /api/v1/public/**} which is {@code permitAll()} at the
     * URL filter level, so the Spring Security filter chain passes the request through.
     * {@code @PreAuthorize("isAuthenticated()")} then raises an {@code AuthorizationDeniedException},
     * which is handled by the {@code AccessDeniedHandler} → 403 Forbidden.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 403 Forbidden when caller is anonymous")
    void search_withAnonymousCaller_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/public/directory")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that GET /api/v1/public/directory with an authenticated client returns 200 OK with default pagination settings.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 200 OK with default pagination when authenticated")
    void search_withDefaultParameters_shouldReturn200OkWithDefaultPagination() throws Exception {
        PaginatedResponse<ArtisanDirectoryCardDTO> emptyResponse = PaginatedResponse.<ArtisanDirectoryCardDTO>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .last(true)
                .build();

        when(directorySearchService.search(any(DirectorySearchFilterDTO.class))).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.last").value(true));

        ArgumentCaptor<DirectorySearchFilterDTO> filterCaptor = ArgumentCaptor.forClass(DirectorySearchFilterDTO.class);
        verify(directorySearchService).search(filterCaptor.capture());

        DirectorySearchFilterDTO capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.resolvePage()).isZero();
        assertThat(capturedFilter.resolveSize()).isEqualTo(20);
        assertThat(capturedFilter.resolveSortBy()).isEqualTo(DirectorySortOrder.Relevance.DEFAULT);
    }

    /**
     * Verifies that all valid query parameters (q, regionSlug, wilayaCode, taxonomies, boolean flags, sorting)
     * bind correctly to the DirectorySearchFilterDTO.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: correctly binds all query parameters to filter DTO")
    void search_withFullParameters_shouldBindAllQueryParametersCorrectly() throws Exception {
        ArtisanDirectoryCardDTO card = ArtisanDirectoryCardDTO.builder()
                .id("artisan-1")
                .artisanName("Djamel Amrani")
                .city("Beni Yenni")
                .wilayaName("Tizi Ouzou")
                .wilayaCode("15")
                .categorySlug("art-du-feu")
                .subCategorySlug("poterie-traditionnelle")
                .rating(4.85)
                .verified(true)
                .premium(true)
                .teacher(true)
                .build();

        PaginatedResponse<ArtisanDirectoryCardDTO> response = PaginatedResponse.<ArtisanDirectoryCardDTO>builder()
                .content(List.of(card))
                .pageNumber(2)
                .pageSize(15)
                .totalElements(45L)
                .totalPages(3)
                .last(false)
                .build();

        when(directorySearchService.search(any(DirectorySearchFilterDTO.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .param("q", "potier")
                        .param("regionSlug", "tizi-ouzou")
                        .param("wilayaCode", "15")
                        .param("categorySlug", "art-du-feu")
                        .param("subCategorySlug", "poterie-traditionnelle")
                        .param("materials", "argile-rouge")
                        .param("techniques", "modelage")
                        .param("epoques", "epoque-ottomane")
                        .param("minRating", "4.5")
                        .param("verifiedOnly", "true")
                        .param("premiumOnly", "true")
                        .param("teacherOnly", "true")
                        .param("sortBy", "RATING_DESC")
                        .param("page", "2")
                        .param("size", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(45))
                .andExpect(jsonPath("$.data.content[0].artisanName").value("Djamel Amrani"));

        ArgumentCaptor<DirectorySearchFilterDTO> filterCaptor = ArgumentCaptor.forClass(DirectorySearchFilterDTO.class);
        verify(directorySearchService).search(filterCaptor.capture());

        DirectorySearchFilterDTO captured = filterCaptor.getValue();
        assertThat(captured.getCleanKeyword()).isEqualTo("potier");
        assertThat(captured.getQ()).isEqualTo("potier");
        assertThat(captured.getRegionSlug()).isEqualTo("tizi-ouzou");
        assertThat(captured.getWilayaCode()).isEqualTo("15");
        assertThat(captured.getCategorySlug()).isEqualTo("art-du-feu");
        assertThat(captured.getSubCategorySlug()).isEqualTo("poterie-traditionnelle");
        assertThat(captured.getMaterials()).containsExactly("argile-rouge");
        assertThat(captured.getTechniques()).containsExactly("modelage");
        assertThat(captured.getEpoques()).containsExactly("epoque-ottomane");
        assertThat(captured.getMinRating()).isEqualTo(4.5);
        assertThat(captured.getVerifiedOnly()).isTrue();
        assertThat(captured.getPremiumOnly()).isTrue();
        assertThat(captured.getTeacherOnly()).isTrue();
        assertThat(captured.getSortBy()).isEqualTo(DirectorySortOrder.Rating.DESC);
        assertThat(captured.getPage()).isEqualTo(2);
        assertThat(captured.getSize()).isEqualTo(15);
    }

    /**
     * Verifies that negative page indexes trigger Jakarta Bean Validation rejection with 422 Unprocessable Content.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 422 Unprocessable Content when page index is negative")
    void search_withNegativePage_shouldReturn422() throws Exception {
        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .param("page", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.page").exists());
    }

    /**
     * Verifies that page size exceeding maximum threshold (100) triggers 422 Unprocessable Content.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 422 Unprocessable Content when page size exceeds 100")
    void search_withOversizedPageSize_shouldReturn422() throws Exception {
        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .param("size", "150")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.size").exists());
    }

    /**
     * Verifies that zero page size triggers 422 Unprocessable Content.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 422 Unprocessable Content when page size is zero")
    void search_withZeroPageSize_shouldReturn422() throws Exception {
        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.size").exists());
    }

    /**
     * Verifies that ratings exceeding 5.0 trigger 422 Unprocessable Content.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 422 Unprocessable Content when minRating exceeds 5.0")
    void search_withInvalidRating_shouldReturn422() throws Exception {
        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .param("minRating", "5.5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.minRating").exists());
    }

    /**
     * Verifies that search keywords exceeding 120 characters trigger 422 Unprocessable Content.
     */
    @Test
    @DisplayName("GET /api/v1/public/directory: returns 422 Unprocessable Content when keyword exceeds 120 characters")
    void search_withOversizedKeyword_shouldReturn422() throws Exception {
        String oversizedKeyword = "a".repeat(125);
        mockMvc.perform(get("/api/v1/public/directory")
                        .with(SecurityTestUtils.client())
                        .param("q", oversizedKeyword)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.keyword").exists());
    }
}
