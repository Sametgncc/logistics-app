package com.logistics.backend.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationMail(String to, String code) {//to-> mail adresi,code->doğrulama kodu
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mail Doğrulama Kodu");
        message.setText("Doğrulama Kodunuz:" + code);

        mailSender.send(message);
    }
}
