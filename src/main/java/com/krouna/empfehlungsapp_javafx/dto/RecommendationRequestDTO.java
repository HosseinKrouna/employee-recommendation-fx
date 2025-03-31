package com.krouna.empfehlungsapp_javafx.dto;

import java.time.LocalDate;
import java.util.List;

public class RecommendationRequestDTO {

    // --- Pflichtfelder ---
    private Long userId;
    private String candidateFirstname;
    private String candidateLastname;
    private String position;
    private String documentCvPath;

    // --- Kontakt & Kennenlernen ---
    private String email;
    private String phone;
    private String knownFrom;
    private LocalDate contactDate;
    private LocalDate convincedCandidateDate;

    // --- Aktuelle oder letzte Position ---
    private String employmentStatus;
    private String currentPosition;
    private String currentCareerLevel;
    private String lastPosition;
    private String lastCareerLevel;

    // --- Informiert über ---
    private boolean informedPosition;
    private boolean informedTasks;
    private boolean informedRequirements;
    private boolean informedClientsProjects;
    private boolean informedBenefits;
    private boolean informedTraining;
    private boolean informedCoach;
    private boolean informedRoles;

    // --- Eckdaten ---
    private Double experienceYears;
    private LocalDate noticePeriod;
    private LocalDate startDate;
    private Integer salaryExpectation;
    private String workHours;
    private Integer travelWillingness;

    // --- Skills (technologisch) ---
    private List<SkillEntry> backendSkills;
    private List<SkillEntry> frontendSkills;
    private List<SkillEntry> databaseSkills;
    private List<SkillEntry> buildSkills;
    private List<SkillEntry> cicdSkills;
    private List<SkillEntry> customSkills;

    // --- Weitere Angaben ---
    private String cvChoice;
    private String cvLink;
    private String personalityType;
    private String hobbies;
    private String projectExperience;
    private String miscellaneous;


    // --- SkillEntry Hilfsklasse ---
    public static class SkillEntry {
        private String name;
        private int percentage;

        public SkillEntry() {}

        public SkillEntry(String name, int percentage) {
            this.name = name;
            this.percentage = percentage;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getPercentage() { return percentage; }
        public void setPercentage(int percentage) { this.percentage = percentage; }
    }


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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getKnownFrom() {
        return knownFrom;
    }

    public void setKnownFrom(String knownFrom) {
        this.knownFrom = knownFrom;
    }

    public LocalDate getContactDate() {
        return contactDate;
    }

    public void setContactDate(LocalDate contactDate) {
        this.contactDate = contactDate;
    }

    public LocalDate getConvincedCandidateDate() {
        return convincedCandidateDate;
    }

    public void setConvincedCandidateDate(LocalDate convincedCandidateDate) {
        this.convincedCandidateDate = convincedCandidateDate;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getCurrentCareerLevel() {
        return currentCareerLevel;
    }

    public void setCurrentCareerLevel(String currentCareerLevel) {
        this.currentCareerLevel = currentCareerLevel;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(String lastPosition) {
        this.lastPosition = lastPosition;
    }

    public String getLastCareerLevel() {
        return lastCareerLevel;
    }

    public void setLastCareerLevel(String lastCareerLevel) {
        this.lastCareerLevel = lastCareerLevel;
    }

    public boolean isInformedPosition() {
        return informedPosition;
    }

    public void setInformedPosition(boolean informedPosition) {
        this.informedPosition = informedPosition;
    }

    public boolean isInformedTasks() {
        return informedTasks;
    }

    public void setInformedTasks(boolean informedTasks) {
        this.informedTasks = informedTasks;
    }

    public boolean isInformedRequirements() {
        return informedRequirements;
    }

    public void setInformedRequirements(boolean informedRequirements) {
        this.informedRequirements = informedRequirements;
    }

    public boolean isInformedClientsProjects() {
        return informedClientsProjects;
    }

    public void setInformedClientsProjects(boolean informedClientsProjects) {
        this.informedClientsProjects = informedClientsProjects;
    }

    public boolean isInformedBenefits() {
        return informedBenefits;
    }

    public void setInformedBenefits(boolean informedBenefits) {
        this.informedBenefits = informedBenefits;
    }

    public boolean isInformedTraining() {
        return informedTraining;
    }

    public void setInformedTraining(boolean informedTraining) {
        this.informedTraining = informedTraining;
    }

    public boolean isInformedCoach() {
        return informedCoach;
    }

    public void setInformedCoach(boolean informedCoach) {
        this.informedCoach = informedCoach;
    }

    public boolean isInformedRoles() {
        return informedRoles;
    }

    public void setInformedRoles(boolean informedRoles) {
        this.informedRoles = informedRoles;
    }

    public Double getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Double experienceYears) {
        this.experienceYears = experienceYears;
    }

    public LocalDate getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(LocalDate noticePeriod) {
        this.noticePeriod = noticePeriod;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getSalaryExpectation() {
        return salaryExpectation;
    }

    public void setSalaryExpectation(Integer salaryExpectation) {
        this.salaryExpectation = salaryExpectation;
    }

    public String getWorkHours() {
        return workHours;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public Integer getTravelWillingness() {
        return travelWillingness;
    }

    public void setTravelWillingness(Integer travelWillingness) {
        this.travelWillingness = travelWillingness;
    }

    public List<SkillEntry> getBackendSkills() {
        return backendSkills;
    }

    public void setBackendSkills(List<SkillEntry> backendSkills) {
        this.backendSkills = backendSkills;
    }

    public List<SkillEntry> getFrontendSkills() {
        return frontendSkills;
    }

    public void setFrontendSkills(List<SkillEntry> frontendSkills) {
        this.frontendSkills = frontendSkills;
    }

    public List<SkillEntry> getDatabaseSkills() {
        return databaseSkills;
    }

    public void setDatabaseSkills(List<SkillEntry> databaseSkills) {
        this.databaseSkills = databaseSkills;
    }

    public List<SkillEntry> getBuildSkills() {
        return buildSkills;
    }

    public void setBuildSkills(List<SkillEntry> buildSkills) {
        this.buildSkills = buildSkills;
    }

    public List<SkillEntry> getCicdSkills() {
        return cicdSkills;
    }

    public void setCicdSkills(List<SkillEntry> cicdSkills) {
        this.cicdSkills = cicdSkills;
    }

    public List<SkillEntry> getCustomSkills() {
        return customSkills;
    }

    public void setCustomSkills(List<SkillEntry> customSkills) {
        this.customSkills = customSkills;
    }

    public String getCvChoice() {
        return cvChoice;
    }

    public void setCvChoice(String cvChoice) {
        this.cvChoice = cvChoice;
    }

    public String getCvLink() {
        return cvLink;
    }

    public void setCvLink(String cvLink) {
        this.cvLink = cvLink;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getProjectExperience() {
        return projectExperience;
    }

    public void setProjectExperience(String projectExperience) {
        this.projectExperience = projectExperience;
    }

    public String getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(String miscellaneous) {
        this.miscellaneous = miscellaneous;
    }
}

