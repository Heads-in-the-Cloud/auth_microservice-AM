package com.ss.training.utopia.auth.security;

import com.ss.training.utopia.auth.dao.UserDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailServiceImpl userDetailService;
    private final UserDao dao;

    public SecurityConfiguration(UserDetailServiceImpl userDetailService, UserDao dao) {
        this.userDetailService = userDetailService;
        this.dao = dao;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // form forgery protection and session management are not needed
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            // add filters for JWT - authenticate, then authorize
            .and()
            .addFilter(new AuthenticationFilter(authenticationManager()))
            .addFilter(new AuthorizationFilter(authenticationManager(), dao))

            // authorize
            .authorizeRequests()
            .antMatchers("/login").permitAll()
            .antMatchers("/api/v1/users/**").authenticated();
    }

    /**
     * create Spring authentication provider for JPA DAOs
     */
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(this.userDetailService);

        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
