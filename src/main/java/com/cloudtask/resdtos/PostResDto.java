package com.cloudtask.resdtos;

import com.cloudtask.entity.Post;

public class PostResDto {
    private String link;
    private String title;
    private String image;
    private String description;
    private String author;
    private String source;
    private long createdAtMLS;
    private long updatedAtMLS;
    private long deletedAtMLS;
    private long publishedAtMLS;
    private int status; // 0.pending | 1. indexed. | -1. deleted.


    public PostResDto() {
    }

    public PostResDto(Post post) {
        this.link = post.getLink();
        this.title = post.getTitle();
        this.image = post.getImage();
        this.description = post.getDescription();
        this.author = post.getAuthor();
        this.source = post.getSource();
        this.createdAtMLS = post.getCreatedAtMLS();
        this.updatedAtMLS = post.getUpdatedAtMLS();
        this.deletedAtMLS = post.getDeletedAtMLS();
        this.publishedAtMLS = post.getPublishedAtMLS();
        this.status = post.getStatus();
    }
}
