package com.udacity.loading


sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}

enum class Status (val status: String) {
    UNKNOWN("Unknown"),
    SUCCESS("Success"),
    FAILURE("Failure")
}