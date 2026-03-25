package com.healthcare.telemedicine.dto;

public class DirectSessionRequest {
    private String patientId;
    private String peerPatientId;

    public DirectSessionRequest() {
    }

    public DirectSessionRequest(String patientId, String peerPatientId) {
        this.patientId = patientId;
        this.peerPatientId = peerPatientId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPeerPatientId() {
        return peerPatientId;
    }

    public void setPeerPatientId(String peerPatientId) {
        this.peerPatientId = peerPatientId;
    }
}
