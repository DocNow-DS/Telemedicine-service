package com.healthcare.telemedicine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "video_sessions")
public class VideoSession {
    @Id
    private String id;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String roomId;
    private String jitsiUrl;
    private Instant startTime;
    private Instant endTime;
    private String endedBy;
    private String notes;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String status; // IN_SESSION, ENDED

    public VideoSession() {
    }

    public VideoSession(String id, String appointmentId, String patientId, String doctorId, String roomId, String jitsiUrl,
                        Instant startTime, Instant endTime, String endedBy, String notes, Instant scheduledAt,
                        Instant createdAt, Instant updatedAt, String status) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.roomId = roomId;
        this.jitsiUrl = jitsiUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.endedBy = endedBy;
        this.notes = notes;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String appointmentId;
        private String patientId;
        private String doctorId;
        private String roomId;
        private String jitsiUrl;
        private Instant startTime;
        private Instant endTime;
        private String endedBy;
        private String notes;
        private Instant scheduledAt;
        private Instant createdAt;
        private Instant updatedAt;
        private String status;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder appointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder patientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder doctorId(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Builder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder jitsiUrl(String jitsiUrl) {
            this.jitsiUrl = jitsiUrl;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder endedBy(String endedBy) {
            this.endedBy = endedBy;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder scheduledAt(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public VideoSession build() {
            return new VideoSession(id, appointmentId, patientId, doctorId, roomId, jitsiUrl,
                    startTime, endTime, endedBy, notes, scheduledAt, createdAt, updatedAt, status);
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    public String getJitsiUrl() {
        return jitsiUrl;
    }

    public void setJitsiUrl(String jitsiUrl) {
        this.jitsiUrl = jitsiUrl;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
