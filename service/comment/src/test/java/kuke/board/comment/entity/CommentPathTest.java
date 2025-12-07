package kuke.board.comment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CommentPathTest {

    @Test
    void createChildCommentTest() {
        // 최초 댓글
        // 00000 <- 생성
        createChildCommentTest(CommentPath.create(""), null, "00000");

        // 00000
        // ㄴ 00000 00000 <- 생성
        createChildCommentTest(CommentPath.create("00000"), null, "0000000000");

        // 00000
        // 00001 <- 생성
        createChildCommentTest(CommentPath.create(""), "00000", "00001");

        // 0000z
        //      abcdz
        //           zzzzz
        //                zzzzz
        //      abce0 <- 생성
        createChildCommentTest(CommentPath.create("0000z"), "0000zabcdzzzzzzzzzzz", "0000zabce0");
    }

    void createChildCommentTest(CommentPath commentPath, String descendantTopPath, String expectedResultPath) {
        CommentPath childCommentPath = commentPath.createChildCommentPath(descendantTopPath);
        assertThat(childCommentPath.getPath()).isEqualTo(expectedResultPath);
    }

    @Test
    void createChildCommentPathMaxDepthTest() {
        assertThatThrownBy(() -> {
            CommentPath.create("zzzzz".repeat(5)).createChildCommentPath(null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createChildCommentPathIfOverFlowTest() {
        // given
        CommentPath commentPath = CommentPath.create("");

        // when, then
        assertThatThrownBy(() -> commentPath.createChildCommentPath("zzzzz"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}