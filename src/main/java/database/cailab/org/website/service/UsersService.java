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

import java.util.List;

import database.cailab.org.website.entity.Users;
import jakarta.servlet.http.HttpServletRequest;

public interface UsersService {
    List<Users> getAllUsers();

    Users getUsersById(Integer id);

    Users getUsersByEmail(String email);

    Users updateUser(Users user);

    public Users updateUsers(Integer id, Users users, String password);

    public Users createUsers(Users users, HttpServletRequest request) throws Exception;

    String requestResetPassword(String email, Users user, String domain) throws Exception;

    boolean validToken(String token, int extraTime);

    void updateNewPassword(String token, String password) throws Exception;

}
