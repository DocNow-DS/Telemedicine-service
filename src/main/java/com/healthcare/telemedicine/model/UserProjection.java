package com.healthcare.telemedicine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_projection")
public class UserProjection {
    @Id
    private String id;

    private String username;
    private String email;
    private Set<String> roles;
    private boolean enabled;

    private Instant lastUpdatedAt;
}
