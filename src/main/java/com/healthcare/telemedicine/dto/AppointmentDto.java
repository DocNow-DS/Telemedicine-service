package com.healthcare.telemedicine.dto;

public class AppointmentDto {
    private String id;
    private String status;
    private String patientId;
    private String doctorId;

    public AppointmentDto() {
    }

    public AppointmentDto(String id, String status, String patientId, String doctorId) {
        this.id = id;
        this.status = status;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
}
