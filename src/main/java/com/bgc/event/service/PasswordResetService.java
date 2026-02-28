package com.bgc.event.service;

public interface PasswordResetService {
    /** Send reset email if user exists. Silent if email not found (anti-enumeration). */
    void requestReset(String email);

    /** Validate token — returns email of the user or throws if invalid/expired */
    String validateToken(String token);

    /** Apply new password and mark token used */
    void resetPassword(String token, String newPassword);
}
