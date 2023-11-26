package com.project.register_mail_confirmation.user;

import com.project.register_mail_confirmation.registration.RegistrationRequest;
import com.project.register_mail_confirmation.registration.token.VerificationToken;

import java.util.List;
import java.util.Optional;

public interface UserServiceImp {

    List<User> getUsers();
    User registerUser(RegistrationRequest request);
    Optional<User> findUserByEmail(String email);

    void saveUserVerificationToken(com.project.register_mail_confirmation.user.User user, java.lang.String verificationToken);

    String validateToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);
}
