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

package database.cailab.org.website.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import database.cailab.org.website.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Integer>{
    //Repository methods
    //return single user or no user found
    Optional<Users> findByEmail(String email);
    
    @Query("SELECT u FROM Users u WHERE u.reset_password_token = ?1")
    Optional<Users> findByReset_password_token(String token);

    //count total number of initial (with same prefix)
    @Query("select count(u) from Users u WHERE u.initials LIKE :initials%")
    int countByInitials(@Param("initials") String initials);
}