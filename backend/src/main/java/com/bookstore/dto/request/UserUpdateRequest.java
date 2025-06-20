package com.bookstore.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    String password;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    String phone;
    String email;
    List<String> roles;
}
