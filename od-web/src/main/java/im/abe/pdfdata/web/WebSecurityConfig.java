package im.abe.pdfdata.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * Disable CSRF protection (unimportant for this app and cumbersome). Allow guest access for all requests.
     *
     * @param http The security config.
     * @throws Exception If something goes wrong in a config method. Never thrown in this class.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .permitAll();
        http.csrf().disable();
    }
}
