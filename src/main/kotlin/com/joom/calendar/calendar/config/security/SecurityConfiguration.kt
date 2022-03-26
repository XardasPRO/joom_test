package com.joom.calendar.calendar.config.security

import com.joom.calendar.calendar.domain.security.JwtTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.csrf.CsrfFilter

@EnableWebSecurity
class SecurityConfiguration(
    private val jwtTokenFilter: JwtTokenFilter
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun configure(http: HttpSecurity?) {
        http.let {
            it!!.authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/test").authenticated()
                ?.antMatchers("/user/create")?.hasAuthority("admin")
//                ?.antMatchers("/calendar")?.hasAnyRole("admin", "user")

            it.sessionManagement()?.sessionCreationPolicy(SessionCreationPolicy.NEVER)
            it.csrf().disable()
            it.addFilterAfter(jwtTokenFilter, CsrfFilter::class.java)
        }
    }
}