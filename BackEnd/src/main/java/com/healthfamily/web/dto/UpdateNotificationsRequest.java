package com.healthfamily.web.dto;

public record UpdateNotificationsRequest(
        boolean inApp,
        boolean email,
        boolean sms
) {}

