package com.healthcare.telemedicine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "video_sessions")
public class VideoSession {
    @Id
    private String id;
    private String appointmentId;
    private String roomId;
    private String jitsiUrl;
    private Instant startTime;
    private Instant endTime;
    private String status; // PENDING, ACTIVE, ENDED

    public VideoSession() {
    }

    public VideoSession(String id, String appointmentId, String roomId, String jitsiUrl, Instant startTime, Instant endTime, String status) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.roomId = roomId;
        this.jitsiUrl = jitsiUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String appointmentId;
        private String roomId;
        private String jitsiUrl;
        private Instant startTime;
        private Instant endTime;
        private String status;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder appointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
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

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public VideoSession build() {
            return new VideoSession(id, appointmentId, roomId, jitsiUrl, startTime, endTime, status);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
