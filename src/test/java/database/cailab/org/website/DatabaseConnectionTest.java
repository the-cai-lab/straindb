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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
//@ActiveProfiles("dev")
public class DatabaseConnectionTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Disabled
    @Test
    public void testDatabaseConnection() {
        // Write a test logic here to interact with the database
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        // Assertions to verify the database connection
        // For example, assert that the count is greater than or equal to 0
        assert (count >= 0);
    }

}
