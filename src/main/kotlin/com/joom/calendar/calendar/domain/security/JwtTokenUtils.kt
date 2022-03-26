package com.joom.calendar.calendar.domain.security

import com.joom.calendar.calendar.model.user.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.crypto.spec.SecretKeySpec

@Component
class JwtTokenUtils {

    @Value("\${spring.security.cookie-token-signing-key}")
    val signingKey: String = "secret"

    @Value("\${spring.security.token-lifetime}")
    var cookieLifetime: Long = 86400

    fun extractUserLoginAndTokenLifeEndDate(token: String): Pair<String, Long> {
        val body = Jwts.parser().setSigningKey(SecretKeySpec(signingKey.toByteArray(), SignatureAlgorithm.HS256.name))
            .parse(token).body as Claims
        return Pair(body["login"].toString(), body["expireAt"].toString().toLong())
    }

    fun generateTokenForUser(user: User): String {
        val expiredAt = LocalDateTime.now().plusSeconds(cookieLifetime).toEpochSecond(ZoneOffset.UTC)
        return Jwts.builder().setPayload("{\"login\":\"${user.login}\",\"expireAt\":\"$expiredAt\"}")
            .signWith(SignatureAlgorithm.HS256, SecretKeySpec(signingKey.toByteArray(), SignatureAlgorithm.HS256.name))
            .compact()
    }
}