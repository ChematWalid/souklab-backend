package com.project.souklab.dto.auth;

import com.project.souklab.model.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequestDTO {

    private String regionId;
    private String region;
    private String city;

    private String bio;
    private String address;
    private String website;
    private String subCategoryId;
    private List<String> materialIds;
    private List<String> epoqueIds;
    private List<String> techniqueIds;

    private ClientType clientType;
    private String companyName;

    public String resolveRegionId() {
        if (regionId != null && !regionId.isBlank()) {
            return regionId;
        }
        return region;
    }
}
