package com.healthcare.telemedicine.event;

import com.healthcare.telemedicine.model.UserProjection;
import com.healthcare.telemedicine.repository.UserProjectionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@ConditionalOnProperty(value = "healthcare.user-events.enabled", havingValue = "true", matchIfMissing = true)
public class UserEventsListener {

    private final UserProjectionRepository userProjectionRepository;

    public UserEventsListener(UserProjectionRepository userProjectionRepository) {
        this.userProjectionRepository = userProjectionRepository;
    }

    @RabbitListener(queues = "${healthcare.user-events.queue}")
    public void onUserEvent(UserEvent event) {
        if (event == null || event.getId() == null) return;

        String type = event.getType() == null ? "" : event.getType();
        if (type.equalsIgnoreCase("USER_DELETED")) {
            userProjectionRepository.deleteById(event.getId());
            return;
        }

        Set<String> roles = event.getRoles() == null ? Set.of() : event.getRoles();

        UserProjection projection = UserProjection.builder()
                .id(event.getId())
                .username(event.getUsername())
                .email(event.getEmail())
                .roles(roles)
                .enabled(event.isEnabled())
                .lastUpdatedAt(event.getOccurredAt() == null ? Instant.now() : event.getOccurredAt())
                .build();

        userProjectionRepository.save(projection);
    }
}
