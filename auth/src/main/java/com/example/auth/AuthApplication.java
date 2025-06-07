package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    SecurityFilterChain mySecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(ae -> ae.anyRequest().authenticated())
                .webAuthn(wa -> wa
                        .rpName("Bootiful Passkeys")
                        .rpId("localhost")
                        .allowedOrigins("http://localhost:9090")
                )
                .formLogin(Customizer.withDefaults())
                .oneTimeTokenLogin(security -> security
                        .tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {
                            var system = "go to http://localhost:9090/login/ott?token=" +
                                    oneTimeToken.getTokenValue();
                            var http1 = "you've got console mail!";
                            System.out.println(system);
                            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                            response.getWriter().println(http1);
                        })
                )
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
