package com.project.souklab.controller.feed;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.dto.feed.FeedPostMediaResponseDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.mock.web.MockMultipartFile;

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
    void publicFeedCanRetrieveOnePost() throws Exception {
        when(feedPostService.getPublic("post-1")).thenReturn(FeedPostResponseDTO.builder().id("post-1").build());

        mockMvc.perform(get("/api/v1/feed/post-1").with(artisan()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("post-1"));
    }

    @Test
    void authenticatedUserCanCreateUpdateRemoveAndManageMedia() throws Exception {
        FeedPostResponseDTO response = FeedPostResponseDTO.builder().id("post-1").build();
        when(feedPostService.create(any())).thenReturn(response);
        when(feedPostService.update(any(), any())).thenReturn(response);
        when(feedPostService.addMedia(any(), any())).thenReturn(FeedPostMediaResponseDTO.builder().id("media-1").build());

        String body = "{\"type\":\"ACTUALITE\",\"title\":\"Title\",\"body\":\"Body\"}";
        mockMvc.perform(post("/api/v1/feed").contentType("application/json").content(body).with(artisan()))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/v1/feed/post-1").contentType("application/json").content(body).with(artisan()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/feed/post-1").with(artisan()))
                .andExpect(status().isOk());
        mockMvc.perform(multipart("/api/v1/feed/post-1/media")
                        .file(new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[]{1}))
                        .with(artisan()))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/v1/feed/post-1/media/media-1").with(artisan()))
                .andExpect(status().isOk());
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

    @Test
    void adminCanPublishHideAndRemovePosts() throws Exception {
        when(feedPostService.publish(any(), any())).thenReturn(FeedPostResponseDTO.builder().id("post-1").build());
        when(feedPostService.hide(any(), any())).thenReturn(FeedPostResponseDTO.builder().id("post-1").build());

        mockMvc.perform(post("/api/v1/admin/feed/post-1/publish")
                        .contentType("application/json").content("{\"note\":\"approved\"}").with(admin()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/feed/post-1/hide")
                        .contentType("application/json").content("{\"note\":\"needs review\"}").with(admin()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/feed/post-1/remove").with(admin()))
                .andExpect(status().isOk());
    }
}
