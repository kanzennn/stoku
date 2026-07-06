package com.example.stoku.domain.model

class UnauthorizedActionException(message: String) : Exception(message)

class InsufficientStockException(message: String) : Exception(message)

class InvalidCredentialsException(message: String) : Exception(message)
