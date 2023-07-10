package br.unirio.gedapp.controller.exceptions

class UserAlreadyInvitedException(
    message: String = "User has already been invited"
) : RuntimeException(message)