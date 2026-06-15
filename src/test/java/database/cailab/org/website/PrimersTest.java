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

import database.cailab.org.website.dto.UserPrimersCountDto;
import database.cailab.org.website.entity.Primers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.PrimersRepository;
import jakarta.transaction.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PrimersTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PrimersRepository primersRepository;

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
        
        //create a dummy primers record
        Primers primers = new Primers();
        primers.setUser(dummyUser);
        primers.setDescription("description");
        primers.setSequence("sequence");
        primers.setComments("comment");
        primers.setLocation("location");
        primers.setMelting_temperature(0);
        primers.setVendor("vendor");
        primers.setConcentration("concentration");
        primers.setPlate_Id(0);
        primers.setWell_id("well id");
        primers.setDate(currentTimestamp);
        primers.setOrientations(null);
        primers.setLab_id("YCp999999998");
        primers.setPersonal_id("TZp999999998");

        Primers primers2 = new Primers();
        primers2.setUser(dummyUser);
        primers2.setDescription("description");
        primers2.setSequence("sequence");
        primers2.setComments("comment");
        primers2.setLocation("location");
        primers2.setMelting_temperature(0);
        primers2.setVendor("vendor");
        primers2.setConcentration("concentration");
        primers2.setPlate_Id(0);
        primers2.setWell_id("well id");
        primers2.setDate(currentTimestamp);
        primers2.setOrientations(null);
        primers2.setLab_id("YCp999999999");
        primers2.setPersonal_id("TZp999999999");

        //save dummy primers record
        Primers savePrimers = entityManager.merge(primers);
        Primers savePrimers2 = entityManager.merge(primers2);

        //Test primers repo: findById
        Optional<Primers> resultFindByID = primersRepository.findById(savePrimers.getId());
        assertThat(resultFindByID).isPresent();
        assertThat(resultFindByID.get()).isEqualTo(savePrimers);
        
        //Test primers repo: findByUser_Id
        List<Primers> resultFindByUserID = primersRepository.findByUser_Id(dummyUser.getId());
        assertThat(resultFindByUserID.size()).isEqualTo(2);
        assertThat(resultFindByUserID.get(0).getDescription()).isEqualTo("description");

        //Test primers repo: countByUser_id
        int numberOfPrimersByUserID = primersRepository.countByUser_id(dummyUser.getId());
        assertThat(numberOfPrimersByUserID).isEqualTo(2);

        //Test primers repo: countByLabID
        int numberOfPrimersByLabID = primersRepository.countByLabID(999999999);
        assertThat(numberOfPrimersByLabID).isEqualTo(1);

        //Test primers repo: findAllByOrderByIdAsc
        List<Primers> primersOrderByAsc = primersRepository.findAllByOrderByIdAsc();
        assertThat(primersOrderByAsc).isNotNull();

        assertThat(primersOrderByAsc).hasSizeGreaterThanOrEqualTo(2);
        //check the last record
        assertThat(primersOrderByAsc.get(primersOrderByAsc.size()-1).getLab_id()).isEqualTo(primers2.getLab_id());
        //check the 2nd last record
        assertThat(primersOrderByAsc.get(primersOrderByAsc.size()-2).getLab_id()).isEqualTo(primers.getLab_id());


        //Test primers repo: findAllByOrderByIdDesc
        List<Primers> pirmersOrderByDesc = primersRepository.findAllByOrderByIdDesc();
        assertThat(pirmersOrderByDesc).isNotNull();
        assertThat(pirmersOrderByDesc).hasSizeGreaterThanOrEqualTo(2);
        
        //check the 1st record
        assertThat(pirmersOrderByDesc.get(0).getLab_id()).isEqualTo(primers2.getLab_id());
        //check the 2nd last record
        assertThat(pirmersOrderByDesc.get(1).getLab_id()).isEqualTo(primers.getLab_id());


        //Test repo:findAllByOrderByUser_IdAsc
        List<Primers> primersOrderByUser_IdAsc = primersRepository.findAllByOrderByUser_IdAsc();
        assertThat(primersOrderByUser_IdAsc).isNotNull();
        assertThat(primersOrderByUser_IdAsc).hasSizeGreaterThanOrEqualTo(2);
        //check the last record
        assertThat(primersOrderByUser_IdAsc.get(primersOrderByUser_IdAsc.size()-1).getLab_id()).isEqualTo(primers2.getLab_id());

        //Test repo: findTopByOrderByIdDesc
        Optional<Primers> primersTopByOrderByIdDesc = primersRepository.findTopByOrderByIdDesc();
        assertThat(primersTopByOrderByIdDesc).isPresent();
        assertThat(primersTopByOrderByIdDesc.get().getLab_id()).isEqualTo(primers2.getLab_id());

        //Test repo: findPrimersCountByUserOrderByName
        List<UserPrimersCountDto> primersUserCount = primersRepository.findPrimersCountByUserOrderByName();
        assertThat(primersUserCount).isNotNull();
        assertThat(primersUserCount).hasSizeGreaterThanOrEqualTo(2);
        assertThat(primersUserCount.get(primersUserCount.size()-1).getName()).isGreaterThanOrEqualTo(primersUserCount.get(0).getName());
    }
}
