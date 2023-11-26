package com.project.register_mail_confirmations.registration;

import com.project.register_mail_confirmations.event.RegistrationCompleteEvent;
import com.project.register_mail_confirmations.event.listner.RegistrationCompleteEventListner;
import com.project.register_mail_confirmations.registration.token.VerificationToken;
import com.project.register_mail_confirmations.registration.token.VerificationTokenRepository;
import com.project.register_mail_confirmations.user.User;
import com.project.register_mail_confirmations.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;
    private final  HttpServletRequest servletRequest;
    private final RegistrationCompleteEventListner eventListner;

    @PostMapping("/register")
    public String registerUser(@RequestBody RegistrationRequest request, final HttpServletRequest httpServletRequest){
        User user = userService.registerUser(request);
        //Publish Registration
        publisher.publishEvent(new RegistrationCompleteEvent(user,applicationUrl(httpServletRequest)));
        return "Success! Please check your email confirm your account";
    }

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token){

        String url = applicationUrl(servletRequest)+"/verification-token-resend?token="+token;
        VerificationToken verifyToken = tokenRepository.findByToken(token);

        if(verifyToken.getUser().isEnabled()){
            return "Account has already been verified";
        }
        String  verificationResult = userService.validateToken(token);

        if(verificationResult.equalsIgnoreCase("valid")){
            return "Email has been verified successful";
        }

        return "invalid Verification Link <a href=\""+url+"\"> New verification link</a>" ;

    }
    @GetMapping("/verification-token-resend")
    public String resendVerificationToken(@RequestParam("token") String oldToken, final HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {

        VerificationToken verifyToken =  userService.generateNewVerificationToken(oldToken);
        var user = verifyToken.getUser();
        resendVerificationTokenEmail(user,applicationUrl(request),verifyToken);

        return "New Verification has been sent to your Email to active your account";
    }

    private void resendVerificationTokenEmail(User user, String applicationUrl, VerificationToken verifyToken) throws MessagingException, UnsupportedEncodingException {

        String url = applicationUrl+"/verifyEmail?token="+verifyToken.getToken();
        eventListner.sendVerificationEmail(url);

    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() +":"+request.getServerPort()+ "/api/v1/auth"+request.getContextPath();
    }
}
