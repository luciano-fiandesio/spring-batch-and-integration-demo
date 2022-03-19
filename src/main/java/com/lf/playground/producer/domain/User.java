package com.lf.playground.producer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private UUID id;
    private Long tenantId;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String downloadPath;

    public String idAsString() {
        return this.id.toString();
    }
}
