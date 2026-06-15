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

package database.cailab.org.website;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.UsersRepository;
import jakarta.transaction.Transactional;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsersTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    UsersRepository usersRepository;

    @Test
    @Transactional
    public void testFindByEmail(){
        //create a dummy user
        Users user = new Users();
        user.setEmail("test999999999@gmail.com");
        user.setEncrypted_password("xxxxxxxxx");
        user.setCurrent_sign_in_ip("0.0.0.0");
        user.setLast_sign_in_ip("0.0.0.0");
        user.setFirstname("Tester");
        user.setLastname("Zero");
        user.setInitials("TZ99999");
        user.setReset_password_token("abcdefghijklmnopq");

        //save dummy user
        Users dummyUser = entityManager.merge(user);
        
        //Test bacteria repo: findByEmail
        Optional<Users> returnuser = usersRepository.findByEmail(user.getEmail());
        assertThat(returnuser).isPresent();
        assertThat(returnuser.get()).isEqualTo(dummyUser);

        Optional<Users> returnuser2 = usersRepository.findByReset_password_token(user.getReset_password_token());
        assertThat(returnuser2).isPresent();
        assertThat(returnuser2.get()).isEqualTo(dummyUser);
        assertThat(returnuser2.get().getReset_password_token()).isEqualTo(user.getReset_password_token());

        int number = usersRepository.countByInitials(user.getInitials());
        assertThat(number).isEqualTo(1);
    }
}
