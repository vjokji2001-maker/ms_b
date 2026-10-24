package com.smartcare.hrms.hiring.service;

import com.smartcare.hrms.hiring.model.entity.CandidateApplication;
import com.smartcare.hrms.hiring.model.entity.CandidateInterviewRound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class InterviewEmailService {

    private @Autowired JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    public void sendInterviewScheduledEmail(
            CandidateApplication candidate,
            CandidateInterviewRound round) {

        String subject = "Interview Scheduled - "
                + candidate.getAppliedPosition();

        String body;

        if (round.getInterviewTime() != null) {
            body = buildInterviewDateTimeEmail(candidate, round);
        } else {
            body = buildInterviewDateOnlyEmail(candidate, round);
        }

        sendEmail(candidate.getEmail(), subject, body);
    }

    public void sendNextRoundEmail(
            CandidateApplication candidate,
            CandidateInterviewRound round) {

        String subject = "Next Interview Round - "
                + candidate.getAppliedPosition();

        String body;

        if (round.getInterviewTime() != null) {
            body = buildNextRoundDateTimeEmail(candidate, round);
        } else {
            body = buildNextRoundDateOnlyEmail(candidate, round);
        }

        sendEmail(candidate.getEmail(), subject, body);
    }

    public void sendSelectedEmail(
            CandidateApplication candidate) {

        String subject = "Congratulations - You Have Been Selected";

        String body =
                "Dear " + candidate.getCandidateName() + ",\n\n" +
                        "Congratulations!\n\n" +
                        "We are pleased to inform you that you have been selected " +
                        "for the position of " +
                        candidate.getAppliedPosition() + ".\n\n" +
                        "Our HR team will contact you regarding the next steps " +
                        "of the joining and onboarding process.\n\n" +
                        "Regards,\n" +
                        "HR Team";

        sendEmail(candidate.getEmail(), subject, body);
    }

    public void sendRejectedEmail(
            CandidateApplication candidate) {

        String subject = "Application Update - "
                + candidate.getAppliedPosition();

        String body =
                "Dear " + candidate.getCandidateName() + ",\n\n" +
                        "Thank you for participating in our selection process " +
                        "for the position of " +
                        candidate.getAppliedPosition() + ".\n\n" +
                        "After careful consideration, we regret to inform you " +
                        "that we will not be proceeding with your application " +
                        "at this time.\n\n" +
                        "We appreciate your interest and wish you all the best " +
                        "in your future endeavors.\n\n" +
                        "Regards,\n" +
                        "HR Team";

        sendEmail(candidate.getEmail(), subject, body);
    }

    private String buildInterviewDateTimeEmail(
            CandidateApplication candidate,
            CandidateInterviewRound round) {

        return
                "Dear " + candidate.getCandidateName() + ",\n\n" +
                        "Your interview has been scheduled.\n\n" +
                        "Position: " + candidate.getAppliedPosition() + "\n" +
                        "Round: " + round.getRoundNumber() + "\n" +
                        "Date: " +
                        round.getInterviewDate().format(DATE_FORMATTER) + "\n" +
                        "Time: " +
                        round.getInterviewTime().format(TIME_FORMATTER) + "\n\n" +
                        "Please be available at the scheduled time.\n\n" +
                        "Regards,\n" +
                        "HR Team";
    }

    private String buildInterviewDateOnlyEmail(
            CandidateApplication candidate,
            CandidateInterviewRound round) {

        return
                "Dear " + candidate.getCandidateName() + ",\n\n" +
                        "Your interview has been scheduled.\n\n" +
                        "Position: " + candidate.getAppliedPosition() + "\n" +
                        "Round: " + round.getRoundNumber() + "\n" +
                        "Date: " +
                        round.getInterviewDate().format(DATE_FORMATTER) + "\n\n" +
                        "The interview time will be communicated to you separately.\n\n" +
                        "Regards,\n" +
                        "HR Team";
    }

    private String buildNextRoundDateTimeEmail(
            CandidateApplication candidate,
            CandidateInterviewRound round) {

        return
                "Dear " + candidate.getCandidateName() + ",\n\n" +
                        "Congratulations on successfully completing your previous " +
                        "interview round.\n\n" +
                        "You have been shortlisted for the next interview round.\n\n" +
                        "Position: " + candidate.getAppliedPosition() + "\n" +
                        "Round: " + round.getRoundNumber() + "\n" +
                        "Date: " +
                        round.getInterviewDate().format(DATE_FORMATTER) + "\n" +
                        "Time: " +
                        round.getInterviewTime().format(TIME_FORMATTER) + "\n\n" +
                        "Please be available at the scheduled time.\n\n" +
                        "Regards,\n" +
                        "HR Team";
    }

    private String buildNextRoundDateOnlyEmail(
            CandidateApplication candidate,
            CandidateInterviewRound round) {

        return
                "Dear " + candidate.getCandidateName() + ",\n\n" +
                        "Congratulations on successfully completing your previous " +
                        "interview round.\n\n" +
                        "You have been shortlisted for the next interview round.\n\n" +
                        "Position: " + candidate.getAppliedPosition() + "\n" +
                        "Round: " + round.getRoundNumber() + "\n" +
                        "Date: " +
                        round.getInterviewDate().format(DATE_FORMATTER) + "\n\n" +
                        "The interview time will be communicated to you separately.\n\n" +
                        "Regards,\n" +
                        "HR Team";
    }

    private void sendEmail(
            String recipient,
            String subject,
            String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}