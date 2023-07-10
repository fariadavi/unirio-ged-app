package br.unirio.gedapp.controller.exceptions

class LastRemainingSystemManagerException(
    message: String = "Can't remove permission to manage permissions from this user since he is the last remaining permission manager in the system"
) : RuntimeException(message)