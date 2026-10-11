package com.example.smartcampus.smartcampus.service.impl;

import com.example.smartcampus.smartcampus.entity.Complaint;
import com.example.smartcampus.smartcampus.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final RestClient restClient = RestClient.create();
    private final String apiKey;
    private final String fromAddress;

    public EmailServiceImpl(
            @Value("${resend.api-key:${RESEND_API_KEY:}}") String apiKey,
            @Value("${resend.from:${EMAIL_FROM:}}") String fromAddress) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
    }

    private void send(String to, String subject, String message) {
        if (apiKey == null || apiKey.isBlank() || fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Email provider configuration missing. Set RESEND_API_KEY and RESEND_FROM or EMAIL_FROM.");
        }

        Map<String, Object> payload = Map.of(
                "from", fromAddress,
                "to", List.of(to),
                "subject", subject,
                "text", message
        );

        restClient.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void sendOtpEmail(String email, String otpCode) {
        send(
                email,
                "Smart Campus Account Verification",
                "Your verification OTP is: " + otpCode + "\nThis OTP expires in 5 minutes."
        );
    }

    @Override
    public void sendPasswordResetOtp(String email, String otpCode) {
        send(
                email,
                "Smart Campus Password Reset",
                "Your password reset OTP is: " + otpCode + "\nThis OTP expires in 5 minutes."
        );
    }

    @Override
    public void sendComplaintAssignNotify(Complaint complaint, String staffEmail) {
        send(
                staffEmail,
                "Complaint Assigned",
                "Hello,\n\n" +
                        "A new complaint has been assigned to you.\n\n" +
                        "Complaint Title: " + complaint.getTitle() + "\n" +
                        "Description: " + complaint.getDescription() + "\n" +
                        "Priority: " + complaint.getComplaintPriority() + "\n" +
                        "Status: " + complaint.getComplaintStatus() + "\n\n" +
                        "Please log in to the Smart Campus Portal to manage it.\n\n" +
                        "Regards,\nSmart Campus Helpdesk"
        );
    }
}
