/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.api.security;

import fr.insalyon.creatis.vip.api.CarminProperties;

import fr.insalyon.creatis.vip.api.security.apikey.SpringApiPrincipal;
import fr.insalyon.creatis.vip.api.security.apikey.ApikeyAuthenticationFilter;
import fr.insalyon.creatis.vip.api.security.apikey.ApikeyAuthenticationProvider;
import fr.insalyon.creatis.vip.core.client.bean.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

import static fr.insalyon.creatis.vip.api.CarminProperties.KEYCLOAK_ACTIVATED;

/**
 * VIP API configuration for apikey and OIDC authentications.
 *
 * Authenticates /rest requests with an API key or an OIDC Bearer token.
 * This is a temporary stub with org.keycloak removed, and OIDC not there yet,
 * so only apikey auth is supported here.
 */

@EnableWebSecurity
@Order(1)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Environment env;
    private final ApikeyAuthenticationProvider apikeyAuthenticationProvider;
    private final VipAuthenticationEntryPoint vipAuthenticationEntryPoint;

    @Autowired
    public ApiSecurityConfig(
            Environment env, ApikeyAuthenticationProvider apikeyAuthenticationProvider,
            VipAuthenticationEntryPoint vipAuthenticationEntryPoint) {
        logger.info("XXX in ApiSecurityConfig constructor: " + vipAuthenticationEntryPoint);
        this.env = env;
        this.apikeyAuthenticationProvider = apikeyAuthenticationProvider;
        this.vipAuthenticationEntryPoint = vipAuthenticationEntryPoint;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        // XXX if(keycloakactive) { ... }
        auth.authenticationProvider(apikeyAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.info("XXX in ApiSecurityConfig.configure: " + http);
        http.antMatcher("/rest/**")
            .authorizeRequests()
                .antMatchers("/rest/platform").permitAll()
                .antMatchers("/rest/authenticate").permitAll()
                .antMatchers("/rest/session").permitAll()
                .regexMatchers("/rest/pipelines\\?public").permitAll()
                .antMatchers("/rest/publications").permitAll()
                .antMatchers("/rest/reset-password").permitAll()
                .antMatchers("/rest/register").permitAll()
                .antMatchers("/rest/executions/{executionId}/summary").hasAnyRole("SERVICE")
                .antMatchers("/rest/simulate-refresh").authenticated()
                .antMatchers("/rest/statistics/**").hasAnyRole("ADVANCED", "ADMINISTRATOR")
                .antMatchers("/rest/**").authenticated()
                .anyRequest().permitAll()
            .and()
            .addFilterBefore(apikeyAuthenticationFilter(), BasicAuthenticationFilter.class)
            .exceptionHandling().authenticationEntryPoint(vipAuthenticationEntryPoint)// also done in parent but needed here when keycloak is not active. It can be done twice without harm.
            // session must be activated otherwise OIDC auth info will be lost when accessing /loginEgi
            //.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .cors().and()
            .headers().frameOptions().sameOrigin().and()
            .csrf().disable();
    }

    @Bean
    public ApikeyAuthenticationFilter apikeyAuthenticationFilter() throws Exception {
        return new ApikeyAuthenticationFilter(
                env.getRequiredProperty(CarminProperties.APIKEY_HEADER_NAME),
                vipAuthenticationEntryPoint, authenticationManager());
    }

    @Service
    public static class CurrentUserProvider implements Supplier<User> {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public User get() {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            logger.info("XXX in ApiSecurityConfig.User.get: " + authentication);
            if (authentication == null) {
                return null;
            }
            User user = getApikeyUser(authentication);
            if (user != null) {
                return user;
            }
            return null; // XXX was getKeycloakUser()
        }

        private User getApikeyUser(Authentication authentication) {
            if ( ! (authentication.getPrincipal() instanceof SpringApiPrincipal)) {
                return null;
            }
            SpringApiPrincipal springCompatibleUser =
                    (SpringApiPrincipal) authentication.getPrincipal();
            return springCompatibleUser.getVipUser();
        }
    }

    /*
        Do not use the default firewall (StrictHttpFirewall) because it blocks
        "//" in url and it is used in gwt rpc calls
     */
    @Bean
    public DefaultHttpFirewall httpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    /**
     * customize roles to match keycloak roles without ROLE_
     */
    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        return mapper;
    }
}
