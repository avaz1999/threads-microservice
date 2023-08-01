package uz.avaz.user

import org.springframework.security.core.context.SecurityContextHolder

fun userId() = SecurityContextHolder.getContext().getUserId()!!
fun userIdOrNull() = SecurityContextHolder.getContext().getUserId()