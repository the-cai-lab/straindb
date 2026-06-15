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

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import database.cailab.org.website.entity.BacterialMarkers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.BacterialMarkersRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import jakarta.transaction.Transactional;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BacteriaMarkerTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    BacterialMarkersRepository bacteriaMarkerrMarkersRepository;

    @Test
    @Transactional
    public void testBacterMarker(){
        //create a dummy marker
        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Create a Timestamp using the current time
        Timestamp currentTimestamp = new Timestamp(currentTimeMillis);
        //create some expect return data
        BacterialMarkers bm1 = new BacterialMarkers();
        bm1.setName("test1");
        bm1.setCreated_at(currentTimestamp);
        bm1.setUpdated_at(currentTimestamp);

        BacterialMarkers bm2 = new BacterialMarkers();
        bm2.setName("test2");
        bm2.setCreated_at(currentTimestamp);
        bm2.setUpdated_at(currentTimestamp);


        BacterialMarkers dummyBacteriaMarker = entityManager.merge(bm1);
        BacterialMarkers dummyBacteriaMarker2 = entityManager.merge(bm2);

        List<BacterialMarkers> bacteriaMarker = bacteriaMarkerrMarkersRepository.findAll();
        assertThat(bacteriaMarker).hasSizeGreaterThan(0);
    }
}
