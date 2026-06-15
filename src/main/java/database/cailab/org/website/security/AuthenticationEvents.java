/*
 * Copyright 2024-2026 The Cai Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package database.cailab.org.website.security;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import database.cailab.org.website.entity.Users;
import database.cailab.org.website.service.UsersService;
import lombok.extern.log4j.Log4j2;

/*
 * Class to handle user login success and login fail action
 * For login success:
 *  do:
 *      update login time
 *      update login count
 *      update login ip
 * For login fail:
 *  do:
 */
@Log4j2
@Component
public class AuthenticationEvents {

    private final UsersService userservice;

    @Autowired
    public AuthenticationEvents(UsersService userservice) {
        this.userservice = userservice;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Users user = (Users) success.getAuthentication().getPrincipal();
        log.info(String.format("User login successfully (%s): ", user.getEmail()));

        // update login info
        int sign_in_count = user.getSign_in_count() + 1;
        Timestamp last_sign_in_at = user.getCurrent_sign_in_at();
        String last_sign_in_ip = user.getCurrent_sign_in_ip();

        Timestamp current_sign_in_at = new Timestamp(success.getTimestamp());
        WebAuthenticationDetails webAuthenticationDetails = (WebAuthenticationDetails) success.getAuthentication()
                .getDetails();
        String current_sign_in_ip = webAuthenticationDetails.getRemoteAddress();

        user.setSign_in_count(sign_in_count);
        user.setLast_sign_in_at(last_sign_in_at);
        user.setLast_sign_in_ip(last_sign_in_ip);
        user.setCurrent_sign_in_at(current_sign_in_at);
        user.setCurrent_sign_in_ip(current_sign_in_ip);

        userservice.updateUser(user);

    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        System.out.println("Login Fail notic...");
    }
}
