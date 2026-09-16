package com.project.souklab.controller.feed;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.service.feed.FeedPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.project.souklab.controller.support.SecurityTestUtils.admin;
import static com.project.souklab.controller.support.SecurityTestUtils.artisan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies public feed routing and administrator moderation authorization.
 */
@ControllerSliceTest(controllers = {FeedPostController.class, AdminFeedController.class})
class FeedPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedPostService feedPostService;

    @Test
    void publicFeedReturnsPublishedPosts() throws Exception {
        when(feedPostService.listPublic(any(), any())).thenReturn(new PageImpl<>(List.of(FeedPostResponseDTO.builder().id("post-1").build())));

        mockMvc.perform(get("/api/v1/feed").with(artisan()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("post-1"));
    }

    @Test
    void nonAdminCannotAccessModerationQueue() throws Exception {
        mockMvc.perform(get("/api/v1/admin/feed/pending").with(artisan()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessModerationQueue() throws Exception {
        when(feedPostService.listPending(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/feed/pending").with(admin()))
                .andExpect(status().isOk());
    }
}
