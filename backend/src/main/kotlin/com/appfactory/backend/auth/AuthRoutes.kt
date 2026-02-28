package com.appfactory.backend.auth

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.AuthProvider
import com.appfactory.domain.port.Credentials
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authProvider: AuthProvider by inject()

    route("/api/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            when (val result = authProvider.signIn(Credentials.EmailPassword(request.email, request.pass))) {
                is DomainResult.Success -> {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "status" to "success",
                            "userId" to result.value.internalUserId.value,
                        )
                    )
                }

                is DomainResult.Failure -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Invalid credentials")
                    )
                }
            }
        }

        post("/logout") {
            when (authProvider.signOut()) {
                is DomainResult.Success -> {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "logged_out"))
                }

                is DomainResult.Failure -> {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Logout failed"))
                }
            }
        }
    }
}

@Serializable
data class LoginRequest(
    val email: String,
    val pass: String,
)
