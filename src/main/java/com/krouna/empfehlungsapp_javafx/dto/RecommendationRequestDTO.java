package com.krouna.empfehlungsapp_javafx.dto;

public class RecommendationRequestDTO {
    private Long userId;
    private String candidateFirstname;
    private String candidateLastname;
    private String position;
    private String documentCvPath;


    // Getter & Setter

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCandidateFirstname() { return candidateFirstname; }
    public void setCandidateFirstname(String candidateFirstname) { this.candidateFirstname = candidateFirstname; }

    public String getCandidateLastname() { return candidateLastname; }
    public void setCandidateLastname(String candidateLastname) { this.candidateLastname = candidateLastname; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getDocumentCvPath() { return documentCvPath; }
    public void setDocumentCvPath(String documentCvPath) { this.documentCvPath = documentCvPath; }


}

