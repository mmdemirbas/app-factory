package com.appfactory.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

const val SUPABASE_JWT_AUTH = "supabase-jwt"

fun Application.configureJwtAuthentication() {
    val jwtSecret = System.getenv("JWT_SECRET")?.takeIf { it.isNotBlank() } ?: "change-me-in-production"
    val supabaseIssuer = System.getenv("SUPABASE_URL")
        ?.takeIf { it.isNotBlank() }
        ?.trimEnd('/')
        ?.let { "$it/auth/v1" }

    if (jwtSecret == "change-me-in-production") {
        environment.log.warn("JWT authentication is using default JWT_SECRET value; set JWT_SECRET in environment")
    }

    install(Authentication) {
        jwt(SUPABASE_JWT_AUTH) {
            realm = "app-factory"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .apply {
                        if (!supabaseIssuer.isNullOrBlank()) {
                            withIssuer(supabaseIssuer)
                        }
                    }
                    .build()
            )
            validate { credential ->
                val subject = credential.payload.subject
                if (subject.isNullOrBlank()) null else io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid or missing bearer token")
                )
            }
        }
    }
}
