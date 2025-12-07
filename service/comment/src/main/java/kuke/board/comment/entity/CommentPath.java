package kuke.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEPTH_CHUNK_SIZE = 5;
    private static final int MAX_DEPTH = 5;

    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    private String path;

    public static CommentPath create(String path) {
        if (isDepthOverFlowed(path)) {
            throw new IllegalArgumentException("Depth overflow");
        }

        CommentPath commentPath = new CommentPath();
        commentPath.path = path;

        return commentPath;
    }

    private static boolean isDepthOverFlowed(String path) {
        return calDepth(path) > MAX_DEPTH;
    }

    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    private static String getIncreaseResult(String lastChunk, int charsetLength) {
        int value = 0;

        for (char c : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(c);
        }

        value++;

        String result = "";
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        return result;
    }

    public int getDepth() {
        return calDepth(path);
    }

    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    public CommentPath createChildCommentPath(String descendantTopPath) {
        if (descendantTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK);
        }

        String childrenTopPath = findChildrenTopPath(descendantTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    private String findChildrenTopPath(String descendantTopPath) {
        return descendantTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);

        if (isChunkOverFlowed(lastChunk)) {
            throw new IllegalArgumentException("chunk overflow");
        }

        int charsetLength = CHARSET.length();
        String result = getIncreaseResult(lastChunk, charsetLength);
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverFlowed(String chunk) {
        return MAX_CHUNK.equals(chunk);
    }
}
