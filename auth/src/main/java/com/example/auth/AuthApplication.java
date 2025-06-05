package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    SecurityFilterChain httpSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .webAuthn(httpSecurityWebAuthnConfigurer -> httpSecurityWebAuthnConfigurer
                        .rpId("localhost")
                        .rpName("Passkeys")
                        .allowedOrigins("http://localhost:9090"))
                .oneTimeTokenLogin(t -> {
                    t.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {
                        System.out.println("click here http://localhost:9090/login/ott?token=" +
                                oneTimeToken.getTokenValue());
                        response.getWriter().write("you've got console mail!");
                        response.getWriter().flush();

                    });
                })
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean
    UserDetailsPasswordService userDetailsPasswordService(
            JdbcUserDetailsManager jdbcUserDetailsManager) {
        return (user, newPassword) -> {
            var u = User.withUserDetails(user).password(newPassword).build();
            jdbcUserDetailsManager.updateUser(u);
            return u;
        };
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

 /*   @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder pw) {
        var rob = User.withUsername("rwinch")
                .password(pw.encode("pw"))
                .roles("USER", "ADMIN")
                .build();
        var jlong = User.withUsername("jlong")
                .password(pw.encode("pw"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(rob, jlong);
    }

*/
}

//@Controller
//@ResponseBody
//class MeController {
//
//    @GetMapping("/")
//    Map<String, String> me(Principal principal) {
//        return Map.of("name", principal.getName());
//    }
//}
