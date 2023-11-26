package com.project.register_mail_confirmation.user;

import com.project.register_mail_confirmation.exceptions.UserAlreadyExistsException;
import com.project.register_mail_confirmation.registration.RegistrationRequest;
import com.project.register_mail_confirmation.registration.token.VerificationToken;
import com.project.register_mail_confirmation.registration.token.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceImp{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationRequest request) {
        Optional<User> user = this.findUserByEmail(request.email());
        if(user.isPresent()){
            throw new UserAlreadyExistsException("User with email "+ request.email() + " already exists");
        }

        var newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUserVerificationToken(User user, String verificationToken) {

        var  token = new VerificationToken(verificationToken,user);

       tokenRepository.save(token);



    }

    @Override
    public String validateToken(String token) {

        VerificationToken verifyToken = tokenRepository.findByToken(token);

        if(verifyToken == null ){
            return "invalid token";
        }
        User user = verifyToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if(verifyToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0){
            //tokenRepository.delete(verifyToken);
            return "Token Already Expired";
        }
        user.setEnabled(true);
        userRepository.save(user);

        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {

        VerificationToken verificationToken = tokenRepository.findByToken(oldToken);
        var verificationTokenTime =new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpirationTime(verificationTokenTime.getTokenExpirationTime());

        return tokenRepository.save(verificationToken);
    }
}
