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

package database.cailab.org.website.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import database.cailab.org.website.csvmapper.PrimersCsvMapper;
import database.cailab.org.website.dto.UserPrimersCountDto;
import database.cailab.org.website.entity.Orientations;
import database.cailab.org.website.entity.Primers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.PrimersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PrimersServiceImpl implements PrimersService {
    private final PrimersRepository primersRepository;
    private final GenerateLabIDService generateLabIDService;
    private final FileUploadService fileUploadService;
    private final CsvHandlerService csvHandlerService;
    private final PerlValidationHandlerService perlValidationHandlerService;
    private final GeneratePersonalIDService generatePersonalIDService;

    @Autowired
    public PrimersServiceImpl(PrimersRepository primersRepository,
            GenerateLabIDService generateLabIDService, FileUploadService fileUploadService,
            CsvHandlerService csvHandlerService, PerlValidationHandlerService perlValidationHandlerService,
            GeneratePersonalIDService generatePersonalIDService) {
        this.primersRepository = primersRepository;
        this.generateLabIDService = generateLabIDService;
        this.fileUploadService = fileUploadService;
        this.csvHandlerService = csvHandlerService;
        this.perlValidationHandlerService = perlValidationHandlerService;
        this.generatePersonalIDService = generatePersonalIDService;
    }

    @Override
    public List<Primers> getAllPrimers() {
        return primersRepository.findAll();
    }

    @Override
    public List<Primers> getAllPrimersOrderByIdAsc() {
        return primersRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<Primers> getAllPrimersOrderByIdDesc() {
        return primersRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Primers> getAllPrimersOrderByUser_IdAsc() {
        return primersRepository.findAllByOrderByUser_IdAsc();
    }

    @Override
    public List<Primers> getAllPrimersByUserId(Integer userID) {
        return primersRepository.findByUser_Id(userID);
    }

    @Override
    public List<UserPrimersCountDto> getUserWithPrimersCount() {
        return primersRepository.findPrimersCountByUserOrderByName();
    }

    @Override
    public Primers getPrimersById(Integer id) {
        return primersRepository.findById(id).orElse(null);
    }

    @Override
    public Primers getPrimersByIdWithUnescapesHTML(Integer id){
        Primers primers = getPrimersById(id);
        if(primers != null){
            /*
             * currently:                                         
             *        Description
             *        Concentration
             *        comments
             *  may contain html special char
            */
            // primers.setHost_strain(StringEscapeUtils.unescapeHtml4(primers.getHost_strain()));
            primers.setDescription(StringEscapeUtils.unescapeHtml4(primers.getDescription()));
            primers.setConcentration(StringEscapeUtils.unescapeHtml4(primers.getConcentration()));
            primers.setComments(StringEscapeUtils.unescapeHtml4(primers.getComments()));
        }
        
        return primers;
    }

    @Override
    public Primers updatePrimers(Integer id, Primers primers) {
        /*
         * You can find some default value at Primers Entity
         */
        
        //check for existing record
        Primers existingPrimers = primersRepository.findById(id).orElseThrow(()->new EntityNotFoundException(String.format("No existing primers record (id: %s) found for update", id)));
        
        //map back the target update record ID
        primers.setId(id);
        
        //these value do not provide at the edit page (some value should use the orginal value)
        primers.setUser(existingPrimers.getUser());
        primers.setCreated_at(existingPrimers.getCreated_at());
        primers.setLab_id(existingPrimers.getLab_id());
        primers.setPersonal_id(existingPrimers.getPersonal_id());
        primers.setSoft_delete(existingPrimers.getSoft_delete());

        //when select empty option, set primers orientations to null
        if(primers.getOrientations().getId() == null){
            primers.setOrientations(null);
        }


        Primers savedPrimers =  primersRepository.save(primers);
        return savedPrimers;
    }


     @Override
    public Primers getLastPrimersRecord() {
        return primersRepository.findTopByOrderByIdDesc().orElse(null);
    }

    /*
     * Use Transactional to prevent 2 user get same last Lab ID and create the same
     * LabID (Lab ID should be unique)
     * Because Lab ID is generate logic is by using last record Lab ID plus + 1
     * (*not from a database sequence)
     */
    @Transactional
    @Override
    public Primers createPrimers(Primers primers, Users user) throws Exception {
        /*
         * You can find some default value at Primers Entity
         */
        
        // prevent concurrent access
        ChemLabConsts.primersCreateLock.lock();
        
        try{
            primers.setUser(user);
            primers.setLab_id(generateLabIDService.getPrimersLabIDList(1).get(0));
            primers.setPersonal_id(generatePersonalIDService.getPrimersPersonalID(1, user.getId(), user.getInitials()).get(0));

        
            if(primers.getOrientations().getId() != null){
                Orientations orientations = new Orientations();
                orientations.setId(primers.getOrientations().getId());
                primers.setOrientations(orientations);
            }
            else{
                primers.setOrientations(null);
            }

            primersRepository.save(primers);

            return primers;

        }finally{
            ChemLabConsts.primersCreateLock.unlock();
        }
    }





        //@Transactional
    @Override
    public List<Primers> createBatchPrimers(MultipartFile file, Users user) throws Exception{
        
        //prevent concurrent access create primers and create batch primers
        ChemLabConsts.primersCreateLock.lock();
        
        try{
            if(!file.isEmpty()){
                
                //upload csv file
                String filename = fileUploadService.saveFile(file, "csv");
                

                 //csv checking
                 StringBuffer outputReport = perlValidationHandlerService.dataValidation(CsvDataType.PRIMERS.getValue(), filename);
                 //System.out.println("output: " + outputReport.toString());
 
                 if(outputReport != null){
                     throw new Exception(outputReport.toString());
                 }

                //csv checking
                List<PrimersCsvMapper> csvHandleRecord = csvHandlerService.readPrimersCsv(filename);

                List<Primers> primersList = new ArrayList<Primers>();
                
                //get the list of primers lab id
                List<String> primersLabIDList = generateLabIDService.getPrimersLabIDList(csvHandleRecord.size());
                
                //get the list of primers personal id
                List<String> primersPersonalIDList = generatePersonalIDService.getPrimersPersonalID(csvHandleRecord.size(), user.getId(), user.getInitials());

                Date currentDate = new Date();
                // Create a Timestamp from the current date
                Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
                
                
                for(int i=0; i<csvHandleRecord.size(); i++){
                    
                    Primers primers = new Primers();
                    primers.setUser(user);
                    primers.setLab_id(primersLabIDList.get(i));
                    primers.setPersonal_id(primersPersonalIDList.get(i));
                    primers.setDescription(csvHandleRecord.get(i).getDescription());
                    primers.setSequence(csvHandleRecord.get(i).getSequence());
                    primers.setMelting_temperature(csvHandleRecord.get(i).getMelting_temperature());
                    primers.setConcentration(csvHandleRecord.get(i).getConcentration());
                    primers.setVendor(csvHandleRecord.get(i).getVendor());
                    // primers.setOrientation_ID(csvHandleRecord.get(i).getOrientation_ID());
                    // primers.setOrientation(csvHandleRecord.get(i).getOrientation_ID());
                    primers.setPlate_Id(csvHandleRecord.get(i).getPlate_Id());
                    primers.setWell_id(csvHandleRecord.get(i).getWell_id());
                    primers.setLocation(csvHandleRecord.get(i).getLocation());
                    primers.setComments(csvHandleRecord.get(i).getComments());
                    primers.setDate(currentTimestamp);
                    if(csvHandleRecord.get(i).getOrientation_id()!=null){
                        Orientations orientations = new Orientations();
                        orientations.setId(csvHandleRecord.get(i).getOrientation_id());
                        primers.setOrientations(orientations); 
                    }
                    
                    primersList.add(primers);

                }
                
                //save into database
                primersRepository.saveAll(primersList);
            }
            
            return null;
        }finally{
            ChemLabConsts.primersCreateLock.unlock();
        }
    }

}
