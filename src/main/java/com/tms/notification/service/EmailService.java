package com.tms.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:noreply@tms.local}")
    private String fromAddress;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    public boolean sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send to: {}, subject: {}", to, subject);
            return true; // Simulate success when disabled
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            return true;

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return false;
        }
    }
}
