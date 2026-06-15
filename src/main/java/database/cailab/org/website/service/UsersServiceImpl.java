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

package database.cailab.org.website.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import database.cailab.org.website.entity.Users;
import database.cailab.org.website.exception.DuplicateEmailException;
import database.cailab.org.website.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class UsersServiceImpl implements UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    @Override
    public Users getUsersById(Integer id) {
        return usersRepository.findById(id).orElse(null);
    }

    public Users getUsersByEmail(String email) {
        return usersRepository.findByEmail(email).orElse(null);
    }

    // ----- Reset password related block-----
    @Override
    public String requestResetPassword(String email, Users user, String domain) throws Exception {
        // only Admin can generate the reset password token
        // user is the current login user (NOT the user account need reset password)
        if (user.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            // check target user exist
            Users targetUser = getUsersByEmail(email);
            if (targetUser != null) {

                // generate reset password token
                String resetPasswordToken = ChemLabConsts.generateToken();

                Date currentDate = new Date();
                // Create a Timestamp from the current date
                Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

                // update into database
                targetUser.setReset_password_token(resetPasswordToken);
                targetUser.setReset_password_sent_at(currentTimestamp);
                updateUser(targetUser);

                String resetPasswordURL = String.format("%s/reset-password?t=%s", domain, resetPasswordToken);

                return resetPasswordURL;
            } else {
                throw new Exception("User cannot be found");
            }
        } else {
            throw new Exception("Permission Denial");
        }

    }

    @Override
    public boolean validToken(String token, int extraTime) {
        Users user = getUsersByRestPasswordToken(token);

        if (user != null
                && timeDifferent(user.getReset_password_sent_at()) <= ChemLabConsts.TOKEN_VAILD_TIME + extraTime) {
            // System.out.println("Time different: " +
            // timeDifferent(user.getReset_password_sent_at()));
            return true;
        } else {
            return false;
        }
    }

    private Users getUsersByRestPasswordToken(String token) {
        return usersRepository.findByReset_password_token(token).orElse(null);
    }

    private long timeDifferent(Timestamp resetPasswordSendAt) {
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
        long timeDifferenceMillis = currentTimestamp.getTime() - resetPasswordSendAt.getTime();
        // Convert milliseconds to minutes
        return (timeDifferenceMillis / (60 * 1000));

    }

    @Override
    public void updateNewPassword(String token, String password) throws Exception {
        boolean isTokeVaild = validToken(token, 10);
        if (!isTokeVaild) {
            throw new Exception("Token expired");
        }

        Users existingUser = getUsersByRestPasswordToken(token);
        existingUser.setEncrypted_password(passwordEncoder.encode(password));

        existingUser.setReset_password_token(null);
        existingUser.setReset_password_sent_at(null);

        usersRepository.save(existingUser);

    }
    // ----- End of Reset password related block -----

    // ----- Update user related block -----
    @Override
    public Users updateUsers(Integer id, Users users, String password) {
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

        // check for existing record
        Users existingUsers = usersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("No existing users record (id: %s) found for update", id)));

        // map back the target update record ID
        users.setId(id);
        users.setCan_edit(existingUsers.getCan_edit());
        users.setApproved(existingUsers.getApproved());
        users.setInitials(existingUsers.getInitials());
        users.setReset_password_token(existingUsers.getReset_password_token());
        users.setReset_password_sent_at(existingUsers.getReset_password_sent_at());
        users.setSign_in_count(existingUsers.getSign_in_count());
        users.setCurrent_sign_in_at(existingUsers.getCurrent_sign_in_at());
        users.setLast_sign_in_at(existingUsers.getLast_sign_in_at());
        users.setCurrent_sign_in_ip(existingUsers.getCurrent_sign_in_ip());
        users.setLast_sign_in_ip(existingUsers.getLast_sign_in_ip());
        users.setCreated_at(existingUsers.getCreated_at());

        
        if(users.getUserRole().getId() == UserRole.TEST.getValue()){
            //Tester
            users.setAdmin(false);
        }else if(users.getUserRole().getId() == UserRole.ADMIN.getValue()){
            //Admin
            users.setAdmin(true);
        }
        
        users.setFirstname(handFirstName(users.getFirstname()));
        users.setLastname(handleLastName(users.getLastname()));

        users.setEmail(users.getEmail().trim());
        
        // set record update time
        users.setUpdated_at(currentTimestamp);
        users.setName(String.format("%s %s", users.getFirstname(), users.getLastname()));

        //handle password change (not allow empty password)
        password = password.trim();
        if (StringUtils.isNotBlank(password)) {
            users.setEncrypted_password(passwordEncoder.encode(password));
        }else{
            users.setEncrypted_password(existingUsers.getEncrypted_password());
        }

        Users savedUsers = updateUser(users);
        return savedUsers;
    }

    @Override
    public Users updateUser(Users user) {
        return usersRepository.save(user);
    }
    // ----- End of update user related block -----

    // ----- Create user related block -----
    @Transactional(rollbackOn = {DuplicateEmailException.class})
    @Override
    public Users createUsers(Users users, HttpServletRequest request) throws Exception {
        /*
         * Yuo can find some default value at Users Entity
         */

        users.setEncrypted_password(passwordEncoder.encode(users.getEncrypted_password()));
        users.setCurrent_sign_in_ip(request.getRemoteAddr());
        users.setLast_sign_in_ip(request.getRemoteAddr());

        if (users.getUserRole().getId() == UserRole.TEST.getValue()) {
            // Tester role
            users.setAdmin(false);
        }else if(users.getUserRole().getId() == UserRole.ADMIN.getValue()){
            //Admin
            users.setAdmin(true);
        }

        users.setFirstname(handFirstName(users.getFirstname()));
        users.setLastname(handleLastName(users.getLastname()));

        try {
            // create user first in order to get the ID
            Users newUser = usersRepository.save(users);

            // generate user initial and then update it
            newUser.setInitials(generateInitials(newUser.getFirstname(), newUser.getLastname(), newUser.getId()));
            newUser = usersRepository.save(newUser);
            return newUser;

        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("Duplicate entry")) {
                throw new DuplicateEmailException("Email already exist. Please use another email");
            } else {
                throw e;
            }
        }
    }

    private String generateInitials(String firstname, String lastname, int id) {
        String initials = firstname.substring(0, 1) + lastname.substring(0, 1);
        int numberOfInitialsExist = usersRepository.countByInitials(initials);

        if (numberOfInitialsExist == 0) {
            // 1st user use this initial
            return initials;
        } else {
            return initials + id;
        }
    }

    // ----- End of create user related block -----


    //handle name
    private String handFirstName(String firstName){
        // upper case of the first letter of the first name and last name
        // first name may contain middle name
        return Arrays.stream(firstName.trim().split(" ")).map(
                s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String handleLastName(String lastName){
        return  lastName.trim().substring(0, 1).toUpperCase() + lastName.trim().substring(1);
    }


}
