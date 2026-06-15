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

import database.cailab.org.website.dto.UserYeastCountDto;
import database.cailab.org.website.entity.Yeast;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.YeastRepository;
import jakarta.transaction.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class YeastTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private YeastRepository yeastRepository;

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
        
        //create a dummy yeast record
        Yeast yeast = new Yeast();
        yeast.setUser(dummyUser);
        yeast.setOther_names("Other names");
        yeast.setPlasmid("plasmid");
        yeast.setGenotype("genotype");
        yeast.setLocation("location");
        yeast.setPlasmid_type("plasmid type");
        yeast.setOther_mating_type("other mating type");
        yeast.setParent_name1("");
        yeast.setParent_name2("");
        yeast.setMarkers_list("markers list");
        yeast.setDate(currentTimestamp);
        yeast.setMating_types(null);
        // yeast.setComments("comments");
        yeast.setComments("");
        yeast.setLab_id("YCy999999998");
        yeast.setPersonal_id("TZy999999998");

        Yeast yeast2 = new Yeast();
        yeast2.setUser(dummyUser);
        yeast2.setOther_names("Other names");
        yeast2.setPlasmid("plasmid");
        yeast2.setGenotype("genotype");
        yeast2.setLocation("location");
        yeast2.setPlasmid_type("plasmid type");
        yeast2.setOther_mating_type("other mating type");
        yeast2.setParent_name1("");
        yeast2.setParent_name2("");
        yeast2.setMarkers_list("markers list");
        yeast2.setDate(currentTimestamp);
        yeast2.setMating_types(null);
        yeast2.setComments("");
        yeast2.setLab_id("YCy999999999");
        yeast2.setPersonal_id("TZy999999999");

        //save dummy yeast record
        Yeast saveYeast = entityManager.merge(yeast);
        Yeast saveYeast2 = entityManager.merge(yeast2);

        //Test yeast repo: findById
        Optional<Yeast> resultFindByID = yeastRepository.findById(saveYeast.getId());
        assertThat(resultFindByID).isPresent();
        assertThat(resultFindByID.get()).isEqualTo(saveYeast);
        
        //Test yeast repo: findByUser_Id
        List<Yeast> resultFindByUserID = yeastRepository.findByUser_Id(dummyUser.getId());
        assertThat(resultFindByUserID.size()).isEqualTo(2);
        assertThat(resultFindByUserID.get(0).getPlasmid()).isEqualTo("plasmid");

        //Test yeast repo: countByUser_id
        int numberOfYeastByUserID = yeastRepository.countByUser_id(dummyUser.getId());
        assertThat(numberOfYeastByUserID).isEqualTo(2);

        //Test yeast repo: countByLabID
        int numberOfYeastByLabID = yeastRepository.countByLabID(999999999);
        assertThat(numberOfYeastByLabID).isEqualTo(1);

        //Test yeast repo: findAllByOrderByIdAsc
        List<Yeast> yeastOrderByAsc = yeastRepository.findAllByOrderByIdAsc();
        assertThat(yeastOrderByAsc).isNotNull();

        assertThat(yeastOrderByAsc).hasSizeGreaterThanOrEqualTo(2);
        //check the last record
        assertThat(yeastOrderByAsc.get(yeastOrderByAsc.size()-1).getLab_id()).isEqualTo(yeast2.getLab_id());
        //check the 2nd last record
        assertThat(yeastOrderByAsc.get(yeastOrderByAsc.size()-2).getLab_id()).isEqualTo(yeast.getLab_id());


        //Test yeast repo: findAllByOrderByIdDesc
        List<Yeast> pirmersOrderByDesc = yeastRepository.findAllByOrderByIdDesc();
        assertThat(pirmersOrderByDesc).isNotNull();
        assertThat(pirmersOrderByDesc).hasSizeGreaterThanOrEqualTo(2);
        
        //check the 1st record
        assertThat(pirmersOrderByDesc.get(0).getLab_id()).isEqualTo(yeast2.getLab_id());
        //check the 2nd last record
        assertThat(pirmersOrderByDesc.get(1).getLab_id()).isEqualTo(yeast.getLab_id());


        //Test repo:findAllByOrderByUser_IdAsc
        List<Yeast> yeastOrderByUser_IdAsc = yeastRepository.findAllByOrderByUser_IdAsc();
        assertThat(yeastOrderByUser_IdAsc).isNotNull();
        assertThat(yeastOrderByUser_IdAsc).hasSizeGreaterThanOrEqualTo(2);
        //check the last record
        assertThat(yeastOrderByUser_IdAsc.get(yeastOrderByUser_IdAsc.size()-1).getLab_id()).isEqualTo(yeast2.getLab_id());

        //Test repo: findTopByOrderByIdDesc
        Optional<Yeast> yeastTopByOrderByIdDesc = yeastRepository.findTopByOrderByIdDesc();
        assertThat(yeastTopByOrderByIdDesc).isPresent();
        assertThat(yeastTopByOrderByIdDesc.get().getLab_id()).isEqualTo(yeast2.getLab_id());

        //Test repo: findYeastCountByUserOrderByName
        List<UserYeastCountDto> yeastUserCount = yeastRepository.findYeastCountByUserOrderByName();
        assertThat(yeastUserCount).isNotNull();
        assertThat(yeastUserCount).hasSizeGreaterThanOrEqualTo(2);
        assertThat(yeastUserCount.get(yeastUserCount.size()-1).getName()).isGreaterThanOrEqualTo(yeastUserCount.get(0).getName());
    }
}
