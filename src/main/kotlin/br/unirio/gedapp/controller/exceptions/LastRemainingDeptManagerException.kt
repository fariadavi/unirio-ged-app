package br.unirio.gedapp.controller.exceptions

class LastRemainingDeptManagerException(
    message: String = "Can't remove permission to manage permissions from this user since he is the last remaining permission manager in this department"
) : RuntimeException(message)