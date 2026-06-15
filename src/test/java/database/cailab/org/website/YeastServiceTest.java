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

import jakarta.transaction.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.annotation.Rollback;

import database.cailab.org.website.entity.Yeast;
import database.cailab.org.website.entity.Mating_types;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.YeastRepository;
import database.cailab.org.website.service.YeastService;
import database.cailab.org.website.service.YeastServiceImpl;
import database.cailab.org.website.service.CsvHandlerService;
import database.cailab.org.website.service.FileUploadService;
import database.cailab.org.website.service.GenerateLabIDService;
import database.cailab.org.website.service.GeneratePersonalIDService;
import database.cailab.org.website.service.PerlValidationHandlerService;


@Transactional
public class YeastServiceTest {
    @Mock
    private YeastRepository yeastRepository;
    
    @Mock
    private GenerateLabIDService generateLabIDService;

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private CsvHandlerService csvHandlerService;

    @Mock
    private PerlValidationHandlerService perlValidationHandlerService;

    @Mock
    private GeneratePersonalIDService generatePersonalIDService;

    @InjectMocks
    private YeastService yeastService;
    
    @BeforeEach 
    public void initMocks() {
        yeastRepository = mock(YeastRepository.class);
        generateLabIDService = mock(GenerateLabIDService.class);
        generatePersonalIDService = mock(GeneratePersonalIDService.class);
        yeastService = new YeastServiceImpl(yeastRepository, generateLabIDService, fileUploadService, csvHandlerService, perlValidationHandlerService, generatePersonalIDService);
    }

    @Test
    @Rollback
    public void testCreateYeast(){
        //Test create yeast 
        
        //create a dummy user
        Users user = new Users();
        user.setId(99999);
        user.setEmail("test999999999@gmail.com");
        user.setEncrypted_password("xxxxxxxxx");
        user.setCurrent_sign_in_ip("0.0.0.0");
        user.setLast_sign_in_ip("0.0.0.0");
        user.setFirstname("Tester");
        user.setLastname("Zero");
        user.setInitials("TZ99999");

        long currentTimeMillis = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTimeMillis);            
        
        //create a dummy yeast record
        Yeast yeast = new Yeast();
        Mating_types mating_types = new Mating_types();
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
        yeast.setMating_types(mating_types);
        yeast.setComments("");
        
        List<String> labIDList = new ArrayList<String>();
        labIDList.add("YCy999999");
        labIDList.add("YCy999998");

        List<String> personalIDList = new ArrayList<String>();
        personalIDList.add("TZy999999");
        personalIDList.add("TZy99998");

        when(yeastRepository.countByUser_id(user.getId())).thenReturn(1);
        
        
        try{
            when(generateLabIDService.getYeastLabIDList(anyInt())).thenReturn(labIDList);
            when(generatePersonalIDService.getYeastPersonalID(anyInt(), anyInt(), anyString())).thenReturn(personalIDList);
        }catch(Exception e){
            
        }
        //Yeast savedYeast = null;
        //Exception exception = assertThrows(Exception.class, () -> {
        try{
            Yeast savedYeast = yeastService.createYeast(yeast, user);
            assertThat(savedYeast).isNotNull();
            assertThat(savedYeast.getUser().getEmail()).isEqualTo(user.getEmail());
            assertThat(savedYeast.getPersonal_id()).isNotEmpty();
            assertThat(savedYeast.getPersonal_id()).isEqualTo("TZy999999");
            assertThat(savedYeast.getLab_id()).isEqualTo("YCy999999");
        }catch(Exception e){
            //You should know Exception always contains contain
            //The purpose of this wrong assert is use to display exception message in concole. (Lazy way)
            assertThat(e.getMessage()).isEmpty();
        }


    }

    @Test
    @Rollback
    public void testGetYeastByIdWithUnescapesHTML(){
        //Test Get Yeast By Id Without escapes HTML tag
        
        //create a dummy yeast record
        Yeast yeast = new Yeast();
        yeast.setOther_names("Other names");
        yeast.setPlasmid("plasmid");
        yeast.setGenotype("genotype");
        yeast.setLocation("location");
        yeast.setPlasmid_type("plasmid type");
        yeast.setOther_mating_type("other mating type");
        yeast.setParent_name1("");
        yeast.setParent_name2("");
        yeast.setMarkers_list("markers list");
        yeast.setMating_types(null);
        yeast.setComments("");
        
        when(yeastRepository.findById(anyInt())).thenReturn(Optional.of(yeast));
        
        
        Yeast returnYeast = yeastService.getYeastByIdWithUnescapesHTML(1);
        assertThat(returnYeast).isNotNull();
        // assertThat(returnYeast.getHost_strain()).isEqualTo(yeast.getHost_strain());
        assertThat(returnYeast.getComments()).isEqualTo(yeast.getComments());
        
    }

    @Test
    @Rollback
    public void testUpdateYeast(){
        try{
            //create a dummy user
            Users user = new Users();
            user.setId(99999);
            user.setEmail("test999999999@gmail.com");
            user.setEncrypted_password("xxxxxxxxx");
            user.setCurrent_sign_in_ip("0.0.0.0");
            user.setLast_sign_in_ip("0.0.0.0");
            user.setFirstname("Tester");
            user.setLastname("Zero");
            user.setInitials("TZ99999");
            
            
            String dateString = "2023-12-24 08:00:00";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = dateFormat.parse(dateString);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());

        
            //create a dummy existing yeast record
            Yeast existYeast = new Yeast();
            existYeast.setUser(user);
            existYeast.setCreated_at(timestamp);
            existYeast.setLab_id("YCy999999");
            
            Mating_types mating_types = new Mating_types();
            mating_types.setId(1);

            Yeast editYeast = new Yeast();
                editYeast.setOther_names("Other names");
                editYeast.setPlasmid("plasmid");
                editYeast.setGenotype("genotype");
                editYeast.setLocation("location");
                editYeast.setPlasmid_type("plasmid type");
                editYeast.setOther_mating_type("other mating type");
                editYeast.setParent_name1("");
                editYeast.setParent_name2("");
                editYeast.setMarkers_list("markers list");
                editYeast.setMating_types(mating_types);
                editYeast.setComments("");


            when(yeastRepository.findById(1)).thenReturn(Optional.of(existYeast));
            when(yeastRepository.save(any())).thenReturn(editYeast);

            Yeast savedYeast = yeastService.updateYeast(1, editYeast);

            assertThat(savedYeast).isNotNull();
        }catch(Exception e){
            //You should know Exception always contains contain
            //The purpose of this wrong assert is use to display exception message in concole. (Lazy way)
            assertThat(e.getMessage()).isEmpty();
        }
    }
}
