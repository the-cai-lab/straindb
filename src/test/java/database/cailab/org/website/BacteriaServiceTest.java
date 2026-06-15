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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;

import database.cailab.org.website.entity.Bacteria;
import database.cailab.org.website.entity.BacterialMarkers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.BacteriaRepository;
import database.cailab.org.website.service.BacteriaService;
import database.cailab.org.website.service.BacteriaServiceImpl;
import database.cailab.org.website.service.CsvHandlerService;
import database.cailab.org.website.service.FileUploadService;
import database.cailab.org.website.service.GenerateLabIDService;
import database.cailab.org.website.service.GeneratePersonalIDService;
import database.cailab.org.website.service.PerlValidationHandlerService;


@Transactional
public class BacteriaServiceTest {
    @Mock
    private BacteriaRepository bacteriaRepository;
    
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
    private BacteriaService bacteriaService;
    
    @BeforeEach 
    public void initMocks() {
        bacteriaRepository = mock(BacteriaRepository.class);
        generateLabIDService = mock(GenerateLabIDService.class);
        generatePersonalIDService = mock(GeneratePersonalIDService.class);
        bacteriaService = new BacteriaServiceImpl(bacteriaRepository, generateLabIDService, fileUploadService, csvHandlerService, perlValidationHandlerService, generatePersonalIDService);
    }

    @Test
    @Rollback
    public void testCreateBacteria(){
        //Test create bacteria 
        
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
        
        //create a dummy bacteria record
        Bacteria bacteria = new Bacteria();
        BacterialMarkers bacterialMarkers = new BacterialMarkers();

        bacteria.setPlasmid_name("plasmid name unit test");
        bacteria.setAlternate_name("alternate name unit test");
        bacteria.setHost_strain("host strain");
        bacteria.setComments("comment");
        bacteria.setLocation("location");
        bacteria.setDate(currentTimestamp);
        bacteria.setBacterialMarkers(bacterialMarkers);
        bacteria.setOther_bacterial_marker("other marker");
        
        List<String> labIDList = new ArrayList<String>();
        labIDList.add("YCe999999");
        labIDList.add("YCe999998");

        List<String> personalIDList = new ArrayList<String>();
        personalIDList.add("TZe999999");
        personalIDList.add("TZe999998");

        when(bacteriaRepository.countByUser_id(user.getId())).thenReturn(1);
        
        
        try{
            when(generateLabIDService.getBacteriaLabIDList(anyInt())).thenReturn(labIDList);
            when(generatePersonalIDService.getBacteriaPersonalID(anyInt(), anyInt(), anyString())).thenReturn(personalIDList);
        }catch(Exception e){
            
        }
        //Bacteria savedBacteria = null;
        //Exception exception = assertThrows(Exception.class, () -> {
        try{
            Bacteria savedBacteria = bacteriaService.createBacteria(bacteria, user);
            assertThat(savedBacteria).isNotNull();
            assertThat(savedBacteria.getUser().getEmail()).isEqualTo(user.getEmail());
            assertThat(savedBacteria.getPersonal_id()).isNotEmpty();
            assertThat(savedBacteria.getPersonal_id()).isEqualTo("TZe999999");
            assertThat(savedBacteria.getLab_id()).isEqualTo("YCe999999");
        }catch(Exception e){
            //You should know Exception always contains contain
            //The purpose of this wrong assert is use to display exception message in concole. (Lazy way)
            assertThat(e.getMessage()).isEmpty();
        }


    }

    @Test
    @Rollback
    public void testGetBacteriaByIdWithUnescapesHTML(){
        //Test Get Bacteria By Id Without escapes HTML tag
        
        //create a dummy bacteria record
        Bacteria bacteria = new Bacteria();
        BacterialMarkers bacterialMarkers = new BacterialMarkers();
        bacteria.setPlasmid_name("plasmid name unit test");
        bacteria.setAlternate_name("alternate name unit test");
        bacteria.setHost_strain("<p>host strain</p>");
        bacteria.setComments("<p>comment</p>");
        bacteria.setLocation("location");
        bacteria.setBacterialMarkers(bacterialMarkers);
        bacteria.setOther_bacterial_marker("other marker");
        
        when(bacteriaRepository.findById(anyInt())).thenReturn(Optional.of(bacteria));
        
        
        Bacteria returnBacteria = bacteriaService.getBacteriaByIdWithUnescapesHTML(1);
        assertThat(returnBacteria).isNotNull();
        assertThat(returnBacteria.getHost_strain()).isEqualTo(bacteria.getHost_strain());
        assertThat(returnBacteria.getComments()).isEqualTo(bacteria.getComments());
        
    }

    @Test
    @Rollback
    public void testUpdateBacteria(){
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
            
            MockMultipartFile emptyFile = new MockMultipartFile("file", "", "", new byte[0]);
            
            
            String dateString = "2023-12-24 08:00:00";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = dateFormat.parse(dateString);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());

        
            //create a dummy existing bacteria record
            Bacteria existBacteria = new Bacteria();
            existBacteria.setUser(user);
            existBacteria.setCreated_at(timestamp);
            existBacteria.setLab_id("YCe999999");
            existBacteria.setSoft_delete(false);
            
            BacterialMarkers bacterialMarkers = new BacterialMarkers();
            bacterialMarkers.setId(1);

            Bacteria editBacteria = new Bacteria();
            editBacteria.setPlasmid_name("plasmid name unit test");
            editBacteria.setAlternate_name("alternate name unit test");
            editBacteria.setHost_strain("host strain");
            editBacteria.setComments("comment");
            editBacteria.setLocation("location");
            editBacteria.setBacterialMarkers(bacterialMarkers);
            editBacteria.setOther_bacterial_marker("other marker");


            when(bacteriaRepository.findById(1)).thenReturn(Optional.of(existBacteria));
            when(bacteriaRepository.save(any())).thenReturn(editBacteria);

            Bacteria savedBacteria = bacteriaService.updateBacteria(1, editBacteria, emptyFile);

            assertThat(savedBacteria).isNotNull();
        }catch(Exception e){
            //You should know Exception always contains contain
            //The purpose of this wrong assert is use to display exception message in concole. (Lazy way)
            assertThat(e.getMessage()).isEmpty();
        }
    }
}
