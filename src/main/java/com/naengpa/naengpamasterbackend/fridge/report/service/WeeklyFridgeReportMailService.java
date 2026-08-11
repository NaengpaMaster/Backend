package com.naengpa.naengpamasterbackend.fridge.report.service;

import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportSummary;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyFridgeReportMailService {

    private final JavaMailSender mailSender;
    private final WeeklyFridgeReportMailTemplate mailTemplate;

    public void send(String receiverEmail, List<WeeklyFridgeReportSummary> summaries) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(receiverEmail);
            helper.setSubject(mailTemplate.subject(summaries));
            helper.setText(mailTemplate.html(summaries), true);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new MailSendException("주간 냉장고 리포트 메일 생성에 실패했습니다.", exception);
        }
    }
}
