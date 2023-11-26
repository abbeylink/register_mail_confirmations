package com.project.register_mail_confirmations.registration;

public record RegistrationRequest(

         String firstName,
         String lastName,
         String email,
         String password,
         String role
) {
}
