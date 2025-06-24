package com.plutus.mvp.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(String to, String verificationCode) {
        logger.info("Sending verification email to {} with code: {}", to, verificationCode);
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        logger.info("Sending password reset email to {} with token: {}", to, resetToken);
    }
}