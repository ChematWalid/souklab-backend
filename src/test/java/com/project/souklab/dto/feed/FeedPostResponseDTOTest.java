package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostMedia;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.model.Formation;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedPostResponseDTOTest {

    @Test
    void mapsAuthorFormationAndMediaUsingResolver() {
        User author = User.builder().email("author@example.com").firstName("Amina").lastName("Craft").build();
        author.setId("author-1");
        Formation formation = new Formation();
        formation.setId("formation-1");
        FeedPost post = FeedPost.builder().author(author).type(FeedPostType.FORMATION)
                .title("Title").body("Body").status(FeedPostStatus.PUBLISHED).formation(formation).build();
        post.setId("post-1");
        FeedPostMedia media = FeedPostMedia.builder().storageKey("objects/image.jpg")
                .contentType("image/jpeg").displayOrder(2).build();
        media.setId("media-1");
        post.addMedia(media);

        FeedPostResponseDTO response = FeedPostResponseDTO.from(post, key -> "/files/" + key);
        assertThat(response.getId()).isEqualTo("post-1");
        assertThat(response.getAuthorName()).isEqualTo("Amina Craft");
        assertThat(response.getFormationId()).isEqualTo("formation-1");
        assertThat(response.getMedia()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo("media-1");
            assertThat(item.getUrl()).isEqualTo("/files/objects/image.jpg");
            assertThat(item.getDisplayOrder()).isEqualTo(2);
        });
    }

    @Test
    void fallsBackToEmailAndSupportsMissingOptionalAuthorNamesAndFormation() {
        User author = User.builder().email("author@example.com").firstName(" ").build();
        author.setId("author-1");
        FeedPost post = FeedPost.builder().author(author).type(FeedPostType.ACTUALITE)
                .title("Title").body("Body").status(FeedPostStatus.PENDING).build();

        FeedPostResponseDTO response = FeedPostResponseDTO.from(post);
        assertThat(response.getAuthorName()).isEqualTo("author@example.com");
        assertThat(response.getFormationId()).isNull();
        assertThat(response.getMedia()).isEmpty();
    }
}
