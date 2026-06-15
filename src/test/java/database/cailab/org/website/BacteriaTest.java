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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import database.cailab.org.website.dto.UserBacteriaCountDto;
import database.cailab.org.website.entity.Bacteria;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.BacteriaRepository;
import database.cailab.org.website.service.ApplicationUtils;
import jakarta.transaction.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BacteriaTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BacteriaRepository bacteriaRepository;

    @Test
    @Transactional
    public void testFindById(){
        //create a dummy user
        Users user = new Users();
        user.setEmail("test999999999@gmail.com");
        user.setEncrypted_password("xxxxxxxxx");
        user.setCurrent_sign_in_ip("0.0.0.0");
        user.setLast_sign_in_ip("0.0.0.0");
        user.setFirstname("Tester");
        user.setLastname("Zero");
        user.setInitials("TZ99999");
        //save dummy user
        Users dummyUser = entityManager.merge(user);

        long currentTimeMillis = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTimeMillis);            
        
        //create a dummy bacteria record
        Bacteria bacteria = new Bacteria();
        //bacteria.setId(99999);
        bacteria.setUser(dummyUser);
        bacteria.setPlasmid_name("plasmid name unit test");
        bacteria.setAlternate_name("alternate name unit test");
        bacteria.setHost_strain("host strain");
        bacteria.setComments("comment");
        bacteria.setLocation("location");
        bacteria.setDate(currentTimestamp);
        bacteria.setBacterialMarkers(null);
        bacteria.setOther_bacterial_marker("other marker");
        bacteria.setLab_id("YCe999999998");
        bacteria.setPersonal_id("TZe999999998");

        Bacteria bacteria2 = new Bacteria();
        bacteria2.setUser(dummyUser);
        bacteria2.setPlasmid_name("plasmid name unit test");
        bacteria2.setAlternate_name("alternate name unit test");
        bacteria2.setHost_strain("host strain");
        bacteria2.setComments("comment");
        bacteria2.setLocation("location");
        bacteria2.setDate(currentTimestamp);
        bacteria2.setBacterialMarkers(null);
        bacteria2.setOther_bacterial_marker("other marker");
        bacteria2.setLab_id("YCe999999999");
        bacteria2.setPersonal_id("TZe999999999");

        //save dummy bacteria record
        Bacteria saveBacteria = entityManager.merge(bacteria);
        Bacteria saveBacteria2 = entityManager.merge(bacteria2);

        //Test bacteria repo: findById
        Optional<Bacteria> resultFindByID = bacteriaRepository.findById(saveBacteria.getId());
        assertThat(resultFindByID).isPresent();
        assertThat(resultFindByID.get()).isEqualTo(saveBacteria);
        
        //Test bacteria repo: findByUser_Id
        List<Bacteria> resultFindByUserID = bacteriaRepository.findByUser_Id(dummyUser.getId());
        assertThat(resultFindByUserID.size()).isEqualTo(2);
        assertThat(resultFindByUserID.get(0).getPlasmid_name()).isEqualTo("plasmid name unit test");

        //Test bacteria repo: countByUser_id
        int numberOfBacteriaByUserID = bacteriaRepository.countByUser_id(dummyUser.getId());
        assertThat(numberOfBacteriaByUserID).isEqualTo(2);

        //Test bacteria repo: countByLabID
        int numberOfBacteriaByLabID = bacteriaRepository.countByLabID(999999999);
        assertThat(numberOfBacteriaByLabID).isEqualTo(1);

        //Test bacteria repo: findAllByOrderByIdAsc
        List<Bacteria> bacteriaOrderByAsc = bacteriaRepository.findAllByOrderByIdAsc();
        assertThat(bacteriaOrderByAsc).isNotNull();
        assertThat(bacteriaOrderByAsc).hasSizeGreaterThanOrEqualTo(2);
        //check the last record
        assertThat(bacteriaOrderByAsc.get(bacteriaOrderByAsc.size()-1).getLab_id()).isEqualTo(bacteria2.getLab_id());
        //check the 2nd last record
        assertThat(bacteriaOrderByAsc.get(bacteriaOrderByAsc.size()-2).getLab_id()).isEqualTo(bacteria.getLab_id());


        //Test bacteria repo: findAllByOrderByIdDesc
        List<Bacteria> bacteriaOrderByDesc = bacteriaRepository.findAllByOrderByIdDesc();
        assertThat(bacteriaOrderByDesc).isNotNull();
        assertThat(bacteriaOrderByDesc).hasSizeGreaterThanOrEqualTo(2);
        
        //check the 1st record
        assertThat(bacteriaOrderByDesc.get(0).getLab_id()).isEqualTo(bacteria2.getLab_id());
        //check the 2nd last record
        assertThat(bacteriaOrderByDesc.get(1).getLab_id()).isEqualTo(bacteria.getLab_id());


        //Test repo:findAllByOrderByUser_IdAsc
        List<Bacteria> bacteriaOrderByUser_IdAsc = bacteriaRepository.findAllByOrderByUser_IdAsc();
        assertThat(bacteriaOrderByUser_IdAsc).isNotNull();
        assertThat(bacteriaOrderByUser_IdAsc).hasSizeGreaterThanOrEqualTo(2);
        //check the last record
        assertThat(bacteriaOrderByUser_IdAsc.get(bacteriaOrderByUser_IdAsc.size()-1).getLab_id()).isEqualTo(bacteria2.getLab_id());

        //Test repo: findTopByOrderByIdDesc
        Optional<Bacteria> bacteriaTopByOrderByIdDesc = bacteriaRepository.findTopByOrderByIdDesc();
        assertThat(bacteriaTopByOrderByIdDesc).isPresent();
        assertThat(bacteriaTopByOrderByIdDesc.get().getLab_id()).isEqualTo(bacteria2.getLab_id());

        //Test repo: findBacteriaCountByUserOrderByName
        List<UserBacteriaCountDto> bacteriaUserCount = bacteriaRepository.findBacteriaCountByUserOrderByName();
        assertThat(bacteriaUserCount).isNotNull();
        assertThat(bacteriaUserCount).hasSizeGreaterThanOrEqualTo(2);
        assertThat(bacteriaUserCount.get(bacteriaUserCount.size()-1).getName()).isGreaterThanOrEqualTo(bacteriaUserCount.get(0).getName());

        String personalID = bacteriaRepository.latestRecordPersonalID(dummyUser.getId());
        assertThat(personalID).isEqualTo(bacteria2.getPersonal_id());

        //int numberOfPersonalID = bacteriaRepository.countByPersonalID(999999999, dummyUser.getId());
        //assertThat(numberOfPersonalID).isEqualTo(1);
    }

    @Test
    public void testExtraDigit(){
        try{        
            String inputText = "TZy123A";
            int id = ApplicationUtils.ExtraDigitAndPlusOne(inputText);
            assertThat(id).isEqualTo(124);
        }catch(Exception e){

        }
    }
}
