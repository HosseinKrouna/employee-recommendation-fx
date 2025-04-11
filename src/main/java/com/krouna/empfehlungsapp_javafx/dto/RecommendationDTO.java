package com.krouna.empfehlungsapp_javafx.dto;

public class RecommendationDTO {
    private Long id;
    private String candidateFirstname;
    private String candidateLastname;
    private String position;
    private String documentCvPath;
    private String businessLink;
    private String documentPdfPath;
    private String status;
    private String submittedAt;
    private Long userId;
    private String recommendedByUsername; // Nur zur Anzeige

    public String getBusinessLink() {
        return businessLink;
    }

    public void setBusinessLink(String businessLink) {
        this.businessLink = businessLink;
    }

    public Long getId() {
        return id;
    }

    public String getDocumentPdfPath() {
        return documentPdfPath;
    }

    public void setDocumentPdfPath(String documentPdfPath) {
        this.documentPdfPath = documentPdfPath;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCandidateFirstname() {
        return candidateFirstname;
    }

    public void setCandidateFirstname(String candidateFirstname) {
        this.candidateFirstname = candidateFirstname;
    }

    public String getCandidateLastname() {
        return candidateLastname;
    }

    public void setCandidateLastname(String candidateLastname) {
        this.candidateLastname = candidateLastname;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDocumentCvPath() {
        return documentCvPath;
    }

    public void setDocumentCvPath(String documentCvPath) {
        this.documentCvPath = documentCvPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRecommendedByUsername() {
        return recommendedByUsername;
    }
    public void setRecommendedByUsername(String recommendedByUsername) {
        this.recommendedByUsername = recommendedByUsername;
    }
}
