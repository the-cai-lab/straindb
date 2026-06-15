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

import database.cailab.org.website.entity.Primers;
import database.cailab.org.website.entity.Orientations;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.PrimersRepository;
import database.cailab.org.website.service.PrimersService;
import database.cailab.org.website.service.PrimersServiceImpl;
import database.cailab.org.website.service.CsvHandlerService;
import database.cailab.org.website.service.FileUploadService;
import database.cailab.org.website.service.GenerateLabIDService;
import database.cailab.org.website.service.GeneratePersonalIDService;
import database.cailab.org.website.service.PerlValidationHandlerService;


@Transactional
public class PrimersServiceTest {
    @Mock
    private PrimersRepository primersRepository;
    
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
    private PrimersService primersService;
    
    @BeforeEach 
    public void initMocks() {
        primersRepository = mock(PrimersRepository.class);
        generateLabIDService = mock(GenerateLabIDService.class);
        generatePersonalIDService = mock(GeneratePersonalIDService.class);
        primersService = new PrimersServiceImpl(primersRepository, generateLabIDService, fileUploadService, csvHandlerService, perlValidationHandlerService, generatePersonalIDService);
    }

    @Test
    @Rollback
    public void testCreatePrimers(){
        //Test create primers 
        
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
        
        //create a dummy primers record
        Primers primers = new Primers();
        Orientations orientations = new Orientations();
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
        primers.setOrientations(orientations);
        
        List<String> labIDList = new ArrayList<String>();
        labIDList.add("YCp999999");
        labIDList.add("YCp999998");

        List<String> personalIDList = new ArrayList<String>();
        personalIDList.add("TZp999999");
        personalIDList.add("TZep99998");

        when(primersRepository.countByUser_id(user.getId())).thenReturn(1);
        
        
        try{
            when(generateLabIDService.getPrimersLabIDList(anyInt())).thenReturn(labIDList);
            when(generatePersonalIDService.getPrimersPersonalID(anyInt(), anyInt(), anyString())).thenReturn(personalIDList);
        }catch(Exception e){
            
        }
        //Primers savedPrimers = null;
        //Exception exception = assertThrows(Exception.class, () -> {
        try{
            Primers savedPrimers = primersService.createPrimers(primers, user);
            assertThat(savedPrimers).isNotNull();
            assertThat(savedPrimers.getUser().getEmail()).isEqualTo(user.getEmail());
            assertThat(savedPrimers.getPersonal_id()).isNotEmpty();
            assertThat(savedPrimers.getPersonal_id()).isEqualTo("TZp999999");
            assertThat(savedPrimers.getLab_id()).isEqualTo("YCp999999");
        }catch(Exception e){
            //You should know Exception always contains contain
            //The purpose of this wrong assert is use to display exception message in concole. (Lazy way)
            assertThat(e.getMessage()).isEmpty();
        }


    }

    @Test
    @Rollback
    public void testGetPrimersByIdWithUnescapesHTML(){
        //Test Get Primers By Id Without escapes HTML tag
        
        //create a dummy primers record
        Primers primers = new Primers();
        Orientations orientations = new Orientations();
        primers.setDescription("description");
        primers.setSequence("sequence");
        primers.setComments("comment");
        primers.setLocation("location");
        primers.setMelting_temperature(0);
        primers.setVendor("vendor");
        primers.setConcentration("concentration");
        primers.setPlate_Id(0);
        primers.setWell_id("well id");
        // primers.setDate(currentTimestamp);
        primers.setOrientations(orientations);
        
        when(primersRepository.findById(anyInt())).thenReturn(Optional.of(primers));
        
        
        Primers returnPrimers = primersService.getPrimersByIdWithUnescapesHTML(1);
        assertThat(returnPrimers).isNotNull();
        // assertThat(returnPrimers.getHost_strain()).isEqualTo(primers.getHost_strain());
        assertThat(returnPrimers.getComments()).isEqualTo(primers.getComments());
        
    }

    @Test
    @Rollback
    public void testUpdatePrimers(){
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

        
            //create a dummy existing primers record
            Primers existPrimers = new Primers();
            existPrimers.setUser(user);
            existPrimers.setCreated_at(timestamp);
            existPrimers.setLab_id("YCp999999");
            existPrimers.setSoft_delete(false);
            
            Orientations orientations = new Orientations();
            orientations.setId(1);

            Primers editPrimers = new Primers();
            editPrimers.setDescription("description");
            editPrimers.setSequence("sequence");
            editPrimers.setComments("comment");
            editPrimers.setLocation("location");
            editPrimers.setMelting_temperature(0);
            editPrimers.setVendor("vendor");
            editPrimers.setConcentration("concentration");
            editPrimers.setPlate_Id(0);
            editPrimers.setWell_id("well id");
            editPrimers.setOrientations(orientations);


            when(primersRepository.findById(1)).thenReturn(Optional.of(existPrimers));
            when(primersRepository.save(any())).thenReturn(editPrimers);

            Primers savedPrimers = primersService.updatePrimers(1, editPrimers);

            assertThat(savedPrimers).isNotNull();
        }catch(Exception e){
            //You should know Exception always contains contain
            //The purpose of this wrong assert is use to display exception message in concole. (Lazy way)
            assertThat(e.getMessage()).isEmpty();
        }
    }
}
