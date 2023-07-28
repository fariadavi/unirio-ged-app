package br.unirio.gedapp.service

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.domain.User
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import javax.mail.internet.MimeMessage

@Service
class EmailService(val javaMailSender: JavaMailSender) {

    fun sendUserInvitedEmail(recipient: String, invitingUser: User, invitedDepartment: Department) {
        val msg = javaMailSender.createMimeMessage()

        msg.setRecipients(MimeMessage.RecipientType.TO, recipient)
        msg.subject = "UNIRIO GED App | ${invitedDepartment.name} | Bem-vindo"

        var htmlTemplate: String = Files.readString(Path.of("src/main/resources/email", "template.html"))

        htmlTemplate = htmlTemplate.replace("\${invitingUserName}", invitingUser.firstName ?: "")
        htmlTemplate = htmlTemplate.replace("\${invitingUserEmail}", invitingUser.email)
        htmlTemplate = htmlTemplate.replace("\${departmentAcronym}", invitedDepartment.acronym!!)
        htmlTemplate = htmlTemplate.replace("\${departmentName}", invitedDepartment.name!!)

        msg.setContent(htmlTemplate, "text/html; charset=utf-8")

        javaMailSender.send(msg)
    }
}