package com.example.pppp.viewmodel

sealed class NavigationTarget {
    object Admin : NavigationTarget()
    object User : NavigationTarget()
    object None : NavigationTarget()
}

