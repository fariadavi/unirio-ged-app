package br.unirio.gedapp.service;

import br.unirio.gedapp.domain.Department
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service;

@Service
class EmailService(val javaMailSender: JavaMailSender) {

    fun sendUserInvitedEmail(recipient: String, invitingUser: String, invitedDepartment: Department) {
        val msg = SimpleMailMessage()
        msg.setTo(recipient)

        msg.setSubject("UNIRIO GED App - You have been added to ${invitedDepartment.name}");
        msg.setText("You've been added to department ${invitedDepartment.acronym} by $invitingUser.\n\n" +
                "Log into UNIRIO GED App by accessing https://github.com/fariadavi/unirio-ged-app-frontend/ and signing in with your google account.")

        javaMailSender.send(msg)
    }
}