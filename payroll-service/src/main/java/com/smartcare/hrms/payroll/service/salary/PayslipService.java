package com.smartcare.hrms.payroll.service.salary;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PayslipService {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendPayslip(String htmlContent, String email, String fileName) {

        // ✅ FIX HTML properly
        htmlContent = htmlContent
                .replaceAll("&nbsp;", " ")
                .replaceAll("<br\\s*/?>", "<br/>")
                .replaceAll("<hr\\s*/?>", "<hr/>");   // 🔥 remove img

        // Optional cleanup
        htmlContent = htmlContent.replaceAll("<figure[^>]*>\\s*</figure>", "");
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "http://103.177.84.241:9099/hr/master/");
            builder.toStream(pdfStream);
            builder.run();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@smartcarehis.com");
            helper.setTo(email);
            helper.setSubject("Your Payslip");

            String body = "<html><body>"
                    + "Dear Employee,<br><br>"
                    + "Please find attached your payslip.<br><br>"
                    + "Regards,<br>HR Team"
                    + "</body></html>";

            helper.setText(body, true);

            helper.addAttachment(
                    fileName + ".pdf",
                    new ByteArrayResource(pdfStream.toByteArray())
            );

            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }
}