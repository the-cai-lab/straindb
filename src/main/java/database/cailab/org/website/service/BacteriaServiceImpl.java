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

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import database.cailab.org.website.csvmapper.BacteriaCsvMapper;
import database.cailab.org.website.dto.UserBacteriaCountDto;
import database.cailab.org.website.entity.Bacteria;
import database.cailab.org.website.entity.BacterialMarkers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.BacteriaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class BacteriaServiceImpl implements BacteriaService {
    private final BacteriaRepository bacteriaRepository;
    private final GenerateLabIDService generateLabIDService;
    private final FileUploadService fileUploadService;
    private final CsvHandlerService csvHandlerService;
    private final PerlValidationHandlerService perlValidationHandlerService;
    private final GeneratePersonalIDService generatePersonalIDService;

    @Autowired
    public BacteriaServiceImpl(BacteriaRepository bacteriaRepository,
            GenerateLabIDService generateLabIDService, FileUploadService fileUploadService,
            CsvHandlerService csvHandlerService, PerlValidationHandlerService perlValidationHandlerService,
            GeneratePersonalIDService generatePersonalIDService) {
        this.bacteriaRepository = bacteriaRepository;
        this.generateLabIDService = generateLabIDService;
        this.fileUploadService = fileUploadService;
        this.csvHandlerService = csvHandlerService;
        this.perlValidationHandlerService = perlValidationHandlerService;
        this.generatePersonalIDService = generatePersonalIDService;
    }

    @Override
    public List<Bacteria> getAllBacteria() {
        return bacteriaRepository.findAll();
    }

    @Override
    public List<Bacteria> getAllBacteriaOrderByIdAsc() {
        return bacteriaRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<Bacteria> getAllBacteriaOrderByIdDesc() {
        return bacteriaRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Bacteria> getAllBacteriaOrderByUser_IdAsc() {
        return bacteriaRepository.findAllByOrderByUser_IdAsc();
    }

    @Override
    public List<Bacteria> getAllBacteriaByUserId(Integer userID) {
        return bacteriaRepository.findByUser_Id(userID);
    }

    @Override
    public List<UserBacteriaCountDto> getUserWithBacteriaCount() {
        return bacteriaRepository.findBacteriaCountByUserOrderByName();
    }

    @Override
    public Bacteria getBacteriaById(Integer id) {
        return bacteriaRepository.findById(id).orElse(null);
    }

    @Override
    public Bacteria getBacteriaByIdWithUnescapesHTML(Integer id) {
        Bacteria bacteria = getBacteriaById(id);
        if (bacteria != null) {
            /*
             * currently:
             * host_strain
             * comments
             * may contain html special char
             */
            bacteria.setHost_strain(StringEscapeUtils.unescapeHtml4(bacteria.getHost_strain()));
            bacteria.setComments(StringEscapeUtils.unescapeHtml4(bacteria.getComments()));
        }

        return bacteria;
    }

    @Override
    public Bacteria updateBacteria(Integer id, Bacteria bacteria, MultipartFile file) throws Exception{
        /*
         * You can find some default value at Bacteria Entity
         */
        
        // check for existing record
        Bacteria existingBacteria = bacteriaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("No existing bacteria record (id: %s) found for update", id)));

        // map back the target update record ID
        bacteria.setId(id);

        // these value do not provide at the edit page (some value should use the orginal value)
        bacteria.setUser(existingBacteria.getUser());
        // bacteria.setDate(existingBacteria.getDate());
        bacteria.setCreated_at(existingBacteria.getCreated_at());
        bacteria.setLab_id(existingBacteria.getLab_id());
        bacteria.setPersonal_id(existingBacteria.getPersonal_id());
        bacteria.setSoft_delete(existingBacteria.getSoft_delete());

        // when select empty option, set bacteria Marker to null
        if (bacteria.getBacterialMarkers().getId() == null) {
            bacteria.setBacterialMarkers(null);
        }

        //handle file upload if user select a file to uplaod
        if (!file.isEmpty()) {
            fileUploadService.saveFile(file, existingBacteria.getUser().getId() + File.separator + bacteria.getId().toString());

            bacteria.setAttachment_file_name(file.getOriginalFilename());
            bacteria.setAttachment_content_type(file.getContentType());
            bacteria.setAttachment_file_size(file.getSize());

            Date currentDate = new Date();
            // Create a Timestamp from the current date
            Timestamp currentTimestamp = new Timestamp(currentDate.getTime());  

            bacteria.setAttachment_updated_at(currentTimestamp);
        }else{
            //user only edit the data and doesn't select any file in the edit form. 
            //then it should use the existing file data
           bacteria.setAttachment_file_name(existingBacteria.getAttachment_file_name());
           bacteria.setAttachment_content_type(existingBacteria.getAttachment_content_type());
           bacteria.setAttachment_file_size(existingBacteria.getAttachment_file_size()); 
           bacteria.setAttachment_updated_at(existingBacteria.getAttachment_updated_at());
        }


        Bacteria savedBacteria = bacteriaRepository.save(bacteria);
        return savedBacteria; 
    }

    @Override
    public Bacteria getLastBacteriaRecord() {
        return bacteriaRepository.findTopByOrderByIdDesc().orElse(null);
    }

    /*
     * Use Transactional to prevent 2 user get same last Lab ID and create the same
     * LabID (Lab ID should be unique)
     * Because Lab ID is generate logic is by using last record Lab ID plus + 1
     * (*not from a database sequence)
     */
    @Transactional
    @Override
    public Bacteria createBacteria(Bacteria bacteria, Users user) throws Exception {
        /*
         * You can find some default value at Bacteria Entity
         */

        //prevent concurrent access create bacteria and create batch bacteria
        ChemLabConsts.bacteriaCreateLock.lock();

        try{
            bacteria.setUser(user);
            bacteria.setLab_id(generateLabIDService.getBacteriaLabIDList(1).get(0));
            bacteria.setPersonal_id(generatePersonalIDService.getBacteriaPersonalID(1, user.getId(), user.getInitials()).get(0));

            if(bacteria.getBacterialMarkers().getId() != null){
                BacterialMarkers bacterialMarkers = new BacterialMarkers();
                bacterialMarkers.setId(bacteria.getBacterialMarkers().getId());
                bacteria.setBacterialMarkers(bacterialMarkers);
            }
            else{
                bacteria.setBacterialMarkers(null);
            }


            // System.out.println(String.format("ID: %s", bacteria.getId()));
            // System.out.println(String.format("User ID: %s", bacteria.getUser().getId()));
            // System.out.println(String.format("Plasmid: %s", bacteria.getPlasmid_name()));
            // System.out.println(String.format("Alternate name: %s", bacteria.getAlternate_name()));
            // System.out.println(String.format("Host Strain: %s", bacteria.getHost_strain()));
            // System.out.println(String.format("Comment: %s", bacteria.getComments()));
            // System.out.println(String.format("Location: %s", bacteria.getLocation()));
            // System.out.println(String.format("Date: %s", bacteria.getDate()));
            // System.out.println(String.format("Create at: %s", bacteria.getCreated_at()));
            // System.out.println(String.format("Bacteria marker: %s", bacteria.getBacterialMarkers().getId()));
            // System.out.println(String.format("Other Bacteria marker: %s", bacteria.getOther_bacterial_marker()));
            // System.out.println(String.format("Lab ID: %s", bacteria.getLab_id()));
            // System.out.println(String.format("Personal ID: %s", bacteria.getPersonal_id()));
            // System.out.println(String.format("Edit: %s", bacteria.getEdited()));

            bacteriaRepository.save(bacteria);

            return bacteria;
        }finally{
            ChemLabConsts.bacteriaCreateLock.unlock();
        }
    }
    
    //@Transactional
    @Override
    public List<Bacteria> createBatchBacteria(MultipartFile file, Users user) throws Exception{
        
        //prevent concurrent access create bacteria and create batch bacteria
        ChemLabConsts.bacteriaCreateLock.lock();
        
        try{
            if(!file.isEmpty()){
                
                //upload csv file
                String filename = fileUploadService.saveFile(file, "csv");
                
                //csv checking
                StringBuffer outputReport = perlValidationHandlerService.dataValidation(CsvDataType.BACTERIA.getValue(), filename);
                //System.out.println("output: " + outputReport.toString());

                if(outputReport != null){
                    throw new Exception(outputReport.toString());
                }


                // List<BacteriaCsvMapper> csvRecord = fileUploadService.readCsv(file);
                List<BacteriaCsvMapper> csvHandleRecord = csvHandlerService.readBacteriaCsv(filename);

                //csvRecord.forEach(r -> System.out.println(String.format("This is the value %s, %s, %s, %s, %s, %s, %s", r.getPlasmid_name(), r.getAlternate_name(), r.getComments(), r.getDate(), r.getHost_strain(), r.getLocation(), r.getOther_bacterial_marker())));
                //csvHandleRecord.forEach(r -> System.out.println(String.format("This is the csv handle value %s, %s, %s, %s, %s, %s, %s", r.getPlasmid_name(), r.getAlternate_name(), r.getHost_strain(), r.getBacterial_marker_id(), r.getOther_bacterial_marker(),  r.getLocation(),    r.getComments()  )));
                List<Bacteria> bacteriaList = new ArrayList<Bacteria>();
                
                //get the list of bacteria lab id
                List<String> bacteriaLabIDList = generateLabIDService.getBacteriaLabIDList(csvHandleRecord.size());
                
                //get the list of bacteria personal id
                List<String> bacteriaPersonalIDList = generatePersonalIDService.getBacteriaPersonalID(csvHandleRecord.size(), user.getId(), user.getInitials());
                Date currentDate = new Date();
                // Create a Timestamp from the current date
                Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
                
                
                for(int i=0; i<csvHandleRecord.size(); i++){
                    
                    Bacteria bacteria = new Bacteria();
                    bacteria.setUser(user);
                    bacteria.setLab_id(bacteriaLabIDList.get(i));
                    bacteria.setPersonal_id(bacteriaPersonalIDList.get(i));
                    bacteria.setPlasmid_name(csvHandleRecord.get(i).getPlasmid_name());
                    bacteria.setAlternate_name(csvHandleRecord.get(i).getAlternate_name());
                    bacteria.setGenotype(csvHandleRecord.get(i).getGenotype());
                    bacteria.setComments(csvHandleRecord.get(i).getComments());
                    bacteria.setDate(currentTimestamp);
                    bacteria.setHost_strain(csvHandleRecord.get(i).getHost_strain());
                    bacteria.setLocation(csvHandleRecord.get(i).getLocation());
                    
                    //BacterialMarkers bacterialMarkers = new BacterialMarkers();
                    //bacterialMarkers.setId(csvHandleRecord.get(i).getBacterial_marker_id());
                    if(csvHandleRecord.get(i).getBacterial_marker_id()!=null){
                        BacterialMarkers bacterialMarkers = new BacterialMarkers();
                        bacterialMarkers.setId(csvHandleRecord.get(i).getBacterial_marker_id());
                        bacteria.setBacterialMarkers(bacterialMarkers);  
                        //bacteria.setBacterialMarkers(null);
                    }

                    bacteria.setOther_bacterial_marker(csvHandleRecord.get(i).getOther_bacterial_marker());
                    
                    bacteriaList.add(bacteria);

                }
                
                //save into database
                bacteriaRepository.saveAll(bacteriaList);
            }
            
            return null;
        }finally{
            ChemLabConsts.bacteriaCreateLock.unlock();
        }
    }
    
}
