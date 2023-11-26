package com.project.register_mail_confirmations.event.listner;

import com.project.register_mail_confirmations.event.RegistrationCompleteEvent;
import com.project.register_mail_confirmations.user.User;
import com.project.register_mail_confirmations.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListner implements ApplicationListener<RegistrationCompleteEvent> {

    private User user;
    private final UserService userService;
    private final JavaMailSender mailSender;
    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        //1. Get Newly Register User
         user = event.getUser();
        //2. Create Verification token for the user
         String verificationToken = UUID.randomUUID().toString();
        //3.Save the verification token for the user
        userService.saveUserVerificationToken(user,verificationToken);

        //4. Build the verification Url
         String url = event.getApplicationUrl()+"/verifyEmail?token="+verificationToken;
        //Send the email
       // log.info("Click the link to verify your account: {}",url );
        try {
         sendVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject ="Email Verification";
        String senderName= "User Registration";
        String content ="<p> Hi, " + user.getFirstName()+ ",</p>"+
                "<p> Thank you for registering with us," +
                " Please, follow the link below to complete your registration.</p>"+
                "<a href=\"" + url + "\"> Verify your email to activate your account</a>"+
                "<p> Thank you <br> Abbeylink LTD";

        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom("admin@rashkemsoft.com.ng", senderName);
        messageHelper.setTo(user.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(content, true);
        mailSender.send(message);


    }
}
