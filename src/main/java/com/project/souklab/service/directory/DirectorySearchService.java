package com.project.souklab.service.directory;

import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import com.project.souklab.dto.directory.DirectorySearchFilterDTO;

/**
 * Service contract for searching, discovering, and filtering the public artisan directory.
 * Executes scored full-text queries and faceted filters using Hibernate Search with Elasticsearch,
 * providing seamless fallback to relational JPA specifications when the search cluster is disabled or unavailable.
 */
public interface DirectorySearchService {

    /**
     * Searches the artisan directory matching the supplied query keywords, geographic constraints,
     * taxonomy filters, and accreditation criteria.
     *
     * @param filter validated directory search criteria and pagination options
     * @return paginated response containing matching artisan directory card DTOs
     */
    PaginatedResponse<ArtisanDirectoryCardDTO> search(DirectorySearchFilterDTO filter);
}
