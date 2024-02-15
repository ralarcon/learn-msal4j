package com.ragcdev.learn.msal4j.resourceserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadResourceServerHttpSecurityConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AadOAuth2ResourceServerSecurityConfig  {

//ALL COMMENTED; OPEN ALL ENDPPOINTS WITHOUT EXPLICIT AUTHORIZATION BUT WORKS WITH APPROLE_
//    /**
//     * Add configuration logic as needed.
//     */
//    @Bean
//    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
//         http
//             .authorizeHttpRequests())
//             .authorizeHttpRequests(authorizeRequests ->
//                 authorizeRequests.anyRequest().authenticated())
//             .oauth2ResourceServer(oauth2ResourceServer ->
//                  oauth2ResourceServer.jwt(jwt ->
//                     jwt.decoder(JwtDecoders.fromIssuerLocation("https://login.microsoftonline.com/a5375b4d-1fa6-4999-92d5-a77870bb6540/v2.0"))) //-> Protect all endpoints and verify the issuer but does not work with APPROLE_
//             );
//             // .oauth2ResourceServer( oauth2ResourceServerCustomizer -> oauth2ResourceServerCustomizer.jwt(Customizer.withDefaults())); //-> Protect all endpoints but does not work with APPROLE_

//         return http.build();
//    }

   /**
    * Add configuration logic as needed.
    */
   @SuppressWarnings("removal")
@Bean
   public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
       http.apply(AadResourceServerHttpSecurityConfigurer.aadResourceServer())
               .and()
           .authorizeHttpRequests()
               .anyRequest().authenticated();
       return http.build();
   }


//https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/v2.0
//https://login.microsoftonline.com/a5375b4d-1fa6-4999-92d5-a77870bb6540/v2.0

}
