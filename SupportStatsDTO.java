package com.helpdesk.dto;

public class SupportStatsDTO {

    private Long totalArticles = 0L;
    private Long publishedArticles = 0L;
    private Long draftArticles = 0L;
    private Long archivedArticles = 0L;
    private Long totalViews = 0L;

    // Constructors
    public SupportStatsDTO() {}

    public SupportStatsDTO(Long totalArticles, Long publishedArticles, Long draftArticles,
                           Long archivedArticles, Long totalViews) {
        this.totalArticles = totalArticles != null ? totalArticles : 0L;
        this.publishedArticles = publishedArticles != null ? publishedArticles : 0L;
        this.draftArticles = draftArticles != null ? draftArticles : 0L;
        this.archivedArticles = archivedArticles != null ? archivedArticles : 0L;
        this.totalViews = totalViews != null ? totalViews : 0L;
    }

    // Getters and Setters
    public Long getTotalArticles() {
        return totalArticles != null ? totalArticles : 0L;
    }

    public void setTotalArticles(Long totalArticles) {
        this.totalArticles = totalArticles != null ? totalArticles : 0L;
    }

    public Long getPublishedArticles() {
        return publishedArticles != null ? publishedArticles : 0L;
    }

    public void setPublishedArticles(Long publishedArticles) {
        this.publishedArticles = publishedArticles != null ? publishedArticles : 0L;
    }

    public Long getDraftArticles() {
        return draftArticles != null ? draftArticles : 0L;
    }

    public void setDraftArticles(Long draftArticles) {
        this.draftArticles = draftArticles != null ? draftArticles : 0L;
    }

    public Long getArchivedArticles() {
        return archivedArticles != null ? archivedArticles : 0L;
    }

    public void setArchivedArticles(Long archivedArticles) {
        this.archivedArticles = archivedArticles != null ? archivedArticles : 0L;
    }

    public Long getTotalViews() {
        return totalViews != null ? totalViews : 0L;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews != null ? totalViews : 0L;
    }
}

