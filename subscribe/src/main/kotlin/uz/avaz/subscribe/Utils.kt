package uz.avaz.subscribe

import org.springframework.security.core.context.SecurityContextHolder

fun userId() = SecurityContextHolder.getContext().getUserId()!!