package br.unirio.gedapp.service

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.domain.User
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.mail.internet.MimeMessage

@Service
class EmailService(val javaMailSender: JavaMailSender) {

    fun sendUserInvitedEmail(recipient: String, invitingUser: User, invitedDepartment: Department) {
        val msg = javaMailSender.createMimeMessage()

        msg.setRecipients(MimeMessage.RecipientType.TO, recipient)
        msg.subject = "READ UNIRIO | Bem-vindo ao ${invitedDepartment.acronym}"

        var htmlTemplate: String = this::class.java.getResource("/email/template.html")!!.readText()

        htmlTemplate = htmlTemplate.replace("\${invitingUserName}", invitingUser.fullName)
        htmlTemplate = htmlTemplate.replace("\${invitingUserEmail}", invitingUser.email)
        htmlTemplate = htmlTemplate.replace("\${departmentAcronym}", invitedDepartment.acronym!!)
        htmlTemplate = htmlTemplate.replace("\${departmentName}", invitedDepartment.name!!)

        msg.setContent(htmlTemplate, "text/html; charset=utf-8")

        javaMailSender.send(msg)
    }
}