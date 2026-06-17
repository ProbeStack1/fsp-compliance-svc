package com.probestack.forgesphere.service;

import com.probestack.forgesphere.model.EmailReportRequest;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around the configured {@link JavaMailSender} used to ship
 * compliance/OWASP report emails. The previous revision propagated SendGrid
 * authentication errors as 500 Internal Server Error, which broke the UI's
 * email button whenever the operator hadn't configured a real key.
 *
 * Now:
 *  • {@link SendOutcome} encodes the three possible end-states (SENT, DISABLED,
 *    FAILED) so the controller can return a meaningful payload.
 *  • A configured "noop-disable-mail" password (the application.properties
 *    default for {@code SENDGRID_API_KEY}) is treated as DISABLED at startup
 *    so we never even attempt to talk to SendGrid in that mode.
 *  • Any {@link MailException} thrown by the underlying sender is caught and
 *    turned into a FAILED outcome — the caller never has to deal with a
 *    surprise 500.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final String NOOP_KEY = "noop-disable-mail";

    private final JavaMailSender javaMailSender;
    private final String defaultFrom;
    private final boolean transportDisabled;

    @Autowired
    public MailService(JavaMailSender javaMailSender,
            @Value("${forgesphere.email.from}") String defaultFrom,
            @Value("${forgesphere.email.sendgrid-api-key:" + NOOP_KEY + "}") String sendgridApiKey) {
        this.javaMailSender = javaMailSender;
        this.defaultFrom = defaultFrom;
        this.transportDisabled = sendgridApiKey == null
                || sendgridApiKey.isBlank()
                || NOOP_KEY.equalsIgnoreCase(sendgridApiKey.trim());
        if (this.transportDisabled) {
            log.info("MailService starting in DISABLED mode — SENDGRID_API_KEY is missing or set to '{}' placeholder.", NOOP_KEY);
        }
    }

    public SendOutcome sendReport(EmailReportRequest request, String reportBody) {
        if (javaMailSender == null) {
            log.warn("Mail sender bean is not wired — email transport is unavailable. To: {}", request.getTo());
            return SendOutcome.disabled("Mail sender bean is not configured.");
        }
        if (transportDisabled) {
            log.info("Mail transport disabled (no SendGrid key). Skipping send for {}", request.getTo());
            return SendOutcome.disabled("SendGrid API key is not configured. Set SENDGRID_API_KEY to enable email delivery.");
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
        try {
            javaMailSender.send(message);
            log.info("Report email delivered to {} from {}", request.getTo(), from);
            return SendOutcome.sent("Email queued for delivery to " + request.getTo());
        } catch (MailException ex) {
            String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            log.warn("SendGrid send failed for {}: {}", request.getTo(), detail);
            // Auth-style failures (bad/missing key) are treated as DISABLED so the UI
            // shows a crisp "configure SendGrid" message rather than a generic error.
            boolean looksLikeAuthFailure = detail.toLowerCase().contains("authentication")
                    || detail.toLowerCase().contains("535")
                    || detail.toLowerCase().contains("unauthorized")
                    || detail.toLowerCase().contains("invalid login");
            if (looksLikeAuthFailure) {
                return SendOutcome.disabled("SendGrid rejected credentials: " + detail);
            }
            return SendOutcome.failed(detail);
        } catch (RuntimeException ex) {
            String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            log.warn("Unexpected email send failure for {}: {}", request.getTo(), detail);
            return SendOutcome.failed(detail);
        }
    }

    /** End-state for an attempted send. The controller renders this into the HTTP response. */
    public static final class SendOutcome {
        public enum Status { SENT, EMAIL_DISABLED, FAILED }

        private final Status status;
        private final String message;

        private SendOutcome(Status status, String message) {
            this.status = status;
            this.message = message;
        }
        public static SendOutcome sent(String m)     { return new SendOutcome(Status.SENT, m); }
        public static SendOutcome disabled(String m) { return new SendOutcome(Status.EMAIL_DISABLED, m); }
        public static SendOutcome failed(String m)   { return new SendOutcome(Status.FAILED, m); }

        public Status status() { return status; }
        public String message() { return message; }
        public boolean delivered() { return status == Status.SENT; }
    }
}
