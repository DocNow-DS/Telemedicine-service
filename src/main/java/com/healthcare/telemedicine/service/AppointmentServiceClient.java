package com.healthcare.telemedicine.service;

import com.healthcare.telemedicine.dto.AppointmentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AppointmentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${appointment-service.url}")
    private String appointmentServiceUrl;

    public AppointmentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<AppointmentDto> getAppointment(String appointmentId) {
        try {
            AppointmentDto appointment = restTemplate.getForObject(
                    appointmentServiceUrl + "/" + appointmentId,
                    AppointmentDto.class
            );
            return Optional.ofNullable(appointment);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
