package com.healthcare.telemedicine.dto;

import java.time.Instant;

public class SessionResponse {
    private String id;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String roomName;
    private String sessionUrl;
    private String status;
    private Instant startedAt;
    private Instant endedAt;
    private String endedBy;
    private String notes;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SessionResponse target = new SessionResponse();

        public Builder id(String id) {
            target.setId(id);
            return this;
        }

        public Builder appointmentId(String appointmentId) {
            target.setAppointmentId(appointmentId);
            return this;
        }

        public Builder patientId(String patientId) {
            target.setPatientId(patientId);
            return this;
        }

        public Builder doctorId(String doctorId) {
            target.setDoctorId(doctorId);
            return this;
        }

        public Builder roomName(String roomName) {
            target.setRoomName(roomName);
            return this;
        }

        public Builder sessionUrl(String sessionUrl) {
            target.setSessionUrl(sessionUrl);
            return this;
        }

        public Builder status(String status) {
            target.setStatus(status);
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            target.setStartedAt(startedAt);
            return this;
        }

        public Builder endedAt(Instant endedAt) {
            target.setEndedAt(endedAt);
            return this;
        }

        public Builder endedBy(String endedBy) {
            target.setEndedBy(endedBy);
            return this;
        }

        public Builder notes(String notes) {
            target.setNotes(notes);
            return this;
        }

        public Builder scheduledAt(Instant scheduledAt) {
            target.setScheduledAt(scheduledAt);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            target.setCreatedAt(createdAt);
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            target.setUpdatedAt(updatedAt);
            return this;
        }

        public SessionResponse build() {
            return target;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getSessionUrl() {
        return sessionUrl;
    }

    public void setSessionUrl(String sessionUrl) {
        this.sessionUrl = sessionUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getEndedBy() {
        return endedBy;
    }

    public void setEndedBy(String endedBy) {
        this.endedBy = endedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
