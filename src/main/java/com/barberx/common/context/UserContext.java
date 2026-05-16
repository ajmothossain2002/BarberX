package com.barberx.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pojo to store the authenticated user context extracted from JWT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private Long userId;
    private String email;
    private List<String> roles;
}
