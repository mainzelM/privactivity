package net.privactivity.store.admin;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
}