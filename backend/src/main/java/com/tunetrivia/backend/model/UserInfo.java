package com.tunetrivia.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lightweight DTO representing the authenticated user returned by /api/auth/me
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    private String sub;
    private String email;
    private String name;
    private String picture;
    private List<String> roles;
}
