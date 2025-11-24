package com.halaq.backend.core.security.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    public @NotBlank(message = "Email is required") @Email(message = "Valid email is required") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is required") @Email(message = "Valid email is required") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Password is required") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, one uppercase, one lowercase and one number") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, one uppercase, one lowercase and one number") String password) {
        this.password = password;
    }

    public @NotBlank(message = "First name is required") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is required") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "Last name is required") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Last name is required") String lastName) {
        this.lastName = lastName;
    }

    public @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Valid Moroccan phone number is required") String getPhone() {
        return phone;
    }

    public void setPhone(@Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Valid Moroccan phone number is required") String phone) {
        this.phone = phone;
    }

    public @NotBlank(message = "User type is required") @Pattern(regexp = "CLIENT|BARBER", message = "User type must be CLIENT or BARBER") String getUserType() {
        return userType;
    }

    public void setUserType(@NotBlank(message = "User type is required") @Pattern(regexp = "CLIENT|BARBER", message = "User type must be CLIENT or BARBER") String userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isAcceptTerms() {
        return acceptTerms;
    }

    public void setAcceptTerms(boolean acceptTerms) {
        this.acceptTerms = acceptTerms;
    }

    public boolean isAcceptPrivacy() {
        return acceptPrivacy;
    }

    public void setAcceptPrivacy(boolean acceptPrivacy) {
        this.acceptPrivacy = acceptPrivacy;
    }

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, one uppercase, one lowercase and one number")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Valid Moroccan phone number is required")
    private String phone;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "CLIENT|BARBER", message = "User type must be CLIENT or BARBER")
    private String userType;

    private String username;
    private String language = "fr"; // fr, ar, en
    private boolean acceptTerms = false;
    private boolean acceptPrivacy = false;
}