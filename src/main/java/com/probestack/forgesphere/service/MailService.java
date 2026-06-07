package com.probestack.forgesphere.service;

import com.probestack.forgesphere.model.EmailReportRequest;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender javaMailSender;
    private final String defaultFrom;

    @Autowired
    public MailService(JavaMailSender javaMailSender,
            @Value("${forgesphere.email.from}") String defaultFrom) {
        this.javaMailSender = javaMailSender;
        this.defaultFrom = defaultFrom;
    }

    public boolean sendReport(EmailReportRequest request, String reportBody) {
        if (javaMailSender == null) {
            log.warn("Mail sender bean is not configured. Report not delivered to {}", request.getTo());
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(reportBody);
        String from = Objects.nonNull(request.getFrom()) && !request.getFrom().isBlank()
                ? request.getFrom()
                : defaultFrom;
        if (Objects.nonNull(from) && !from.isBlank()) {
            message.setFrom(from);
        }
        javaMailSender.send(message);
        log.info("Report email delivered to {} from {}", request.getTo(), from);
        return true;
    }
}
