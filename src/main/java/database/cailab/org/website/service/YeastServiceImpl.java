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

import com.mysql.cj.util.StringUtils;

import database.cailab.org.website.csvmapper.YeastCsvMapper;
import database.cailab.org.website.dto.UserYeastCountDto;
import database.cailab.org.website.entity.Mating_types;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.entity.Yeast;
import database.cailab.org.website.repository.YeastRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class YeastServiceImpl implements YeastService {
    private final YeastRepository yeastRepository;
    private final GenerateLabIDService generateLabIDService;
    private final FileUploadService fileUploadService;
    private final CsvHandlerService csvHandlerService;
    private final PerlValidationHandlerService perlValidationHandlerService;
    private final GeneratePersonalIDService generatePersonalIDService;

    @Autowired
    public YeastServiceImpl(YeastRepository yeastRepository,
            GenerateLabIDService generateLabIDService, FileUploadService fileUploadService,
            CsvHandlerService csvHandlerService, PerlValidationHandlerService perlValidationHandlerService,
            GeneratePersonalIDService generatePersonalIDService) {
        this.yeastRepository = yeastRepository;
        this.generateLabIDService = generateLabIDService;
        this.fileUploadService = fileUploadService;
        this.csvHandlerService = csvHandlerService;
        this.perlValidationHandlerService = perlValidationHandlerService;
        this.generatePersonalIDService = generatePersonalIDService;
    }

    @Override
    public List<Yeast> getAllYeast() {
        return yeastRepository.findAll();
    }

    @Override
    public List<Yeast> getAllYeastOrderByIdAsc() {
        return yeastRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<Yeast> getAllYeastOrderByIdDesc() {
        return yeastRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Yeast> getAllYeastOrderByUser_IdAsc() {
        return yeastRepository.findAllByOrderByUser_IdAsc();
    }

    @Override
    public List<Yeast> getAllYeastByUserId(Integer userID) {
        return yeastRepository.findByUser_Id(userID);
    }

    @Override
    public List<UserYeastCountDto> getUserWithYeastCount() {
        return yeastRepository.findYeastCountByUserOrderByName();
    }

    @Override
    public Yeast getYeastById(Integer id) {
        return yeastRepository.findById(id).orElse(null);
    }


    @Override
    public Yeast getYeastByIdWithUnescapesHTML(Integer id){
        Yeast yeast = getYeastById(id);
        if(yeast != null){
            /*
             * currently:                                         
             *        host_strain
             *        comments
             *  may contain html special char
            */
            yeast.setComments(StringEscapeUtils.unescapeHtml4(yeast.getComments()));
        }
        
        return yeast;
    }

    @Override
    public Yeast updateYeast(Integer id, Yeast yeast)  throws Exception {
        /*
         * You can find some default value at Yeast Entity
         */
        
        //check for existing record
        Yeast existingYeast = yeastRepository.findById(id).orElseThrow(()->new EntityNotFoundException(String.format("No existing yeast record (id: %s) found for update", id)));
        
        //map back the target update record ID
        yeast.setId(id);
        
        //these value do not provide at the edit page (some value should use the orginal value)
        yeast.setUser(existingYeast.getUser());
        yeast.setCreated_at(existingYeast.getCreated_at());
        yeast.setLab_id(existingYeast.getLab_id());
        yeast.setPersonal_id(existingYeast.getPersonal_id());
        //yeast.setSoft_delete(yeast.getSoft_delete());
        
        //when select empty option, set mating types to null
        if(yeast.getMating_types().getId() == null){
            yeast.setMating_types(null);
        }

        //handle Parent ID
        //remove white space
        yeast.setParent_name1(yeast.getParent_name1().trim());
        yeast.setParent_name2(yeast.getParent_name2().trim());
        
        //if parent name 1 is not empty , check is it correct parent name
        if(!StringUtils.isEmptyOrWhitespaceOnly(yeast.getParent_name1())){
           checkParentNameExist(yeast.getParent_name1(), "Parent Yeast Id");
        }
        
        //if parent name 2 is not empty , check is it correct parent name
        if(!StringUtils.isEmptyOrWhitespaceOnly(yeast.getParent_name2())){
           checkParentNameExist(yeast.getParent_name2(), "Other Parent Yeast Id");
        }

        Yeast savedYeast =  yeastRepository.save(yeast);
        return savedYeast;

        // boolean hasNonMatchingParent = true;
        // List<String> parent_ids_database = yeastRepository.search_parentId();
        // System.out.print(parent_ids_database);
        // System.out.print(parent_input);
        // for (String parent : parent_ids_database) {
        //     if (parent.equals(parent_input)) {
        //         // System.out.print(parent);
        //         // System.out.print(parent_input);
        //         hasNonMatchingParent = false;
        //         break;
        //     }
        // }
        // System.out.print(hasNonMatchingParent);
        // if (hasNonMatchingParent) {
        //     if((parent_input.equals(""))){
        //     // System.out.print("hiiiiiiiiiiiiiiiiiiiiiiiiii");
        //     hasNonMatchingParent = false;
        //     Yeast savedYeast =  yeastRepository.save(yeast);
        //     return savedYeast;

        // }
        //     else{
        //         throw new InvalidYeastException("Parent input is empty. Cannot save yeast.");
        //         // return existingYeast;
        //     }

        // Yeast savedYeast =  yeastRepository.save(yeast);
        // return savedYeast;
        // }
        // else{
        //         Yeast savedYeast =  yeastRepository.save(yeast);
        //         return savedYeast;
        //         // throw new InvalidYeastException("Parent input is empty. Cannot save yeast.");
        //     }
    
}

    @Transactional
    @Override
    public Yeast createYeast(Yeast yeast, Users user) throws Exception {
        /*
         * You can find some default value at Yeast Entity
         */
        
        // prevent concurrent access
        ChemLabConsts.yeastCreateLock.lock();
        
        try{
            yeast.setUser(user);
            yeast.setLab_id(generateLabIDService.getYeastLabIDList(1).get(0));
            //yeast.setPersonal_id(personalID);
            yeast.setPersonal_id(generatePersonalIDService.getYeastPersonalID(1, user.getId(), user.getInitials()).get(0));

        
            if(yeast.getMating_types().getId() != null){
                Mating_types mating_types = new Mating_types();
                mating_types.setId(yeast.getMating_types().getId());
                yeast.setMating_types(mating_types);
            }
            else{
                yeast.setMating_types(null);
            }

            //handle Parent ID
            //remove white space
            yeast.setParent_name1(yeast.getParent_name1().trim());
            yeast.setParent_name2(yeast.getParent_name2().trim());
        
            //if parent name 1 is not empty , check is it correct parent name
            if(!StringUtils.isEmptyOrWhitespaceOnly(yeast.getParent_name1())){
            checkParentNameExist(yeast.getParent_name1(), "Parent Yeast Id");
            }
        
            //if parent name 2 is not empty , check is it correct parent name
            if(!StringUtils.isEmptyOrWhitespaceOnly(yeast.getParent_name2())){
            checkParentNameExist(yeast.getParent_name2(), "Other Parent Yeast Id");
            }
        
            //System.out.println("This is what i get from the check box: " + yeast.getOther_yeast_marker_id());
            
            //boolean hasNonMatchingParent = true;
            //List<String> parent_ids_database = yeastRepository.search_parentId();
            //find Input parent ID existing or not
            // for (String parent : parent_ids_database) {
            //     if (parent.equals(parent_input)) {
            //         hasNonMatchingParent = false;
            //         break;
            //     }
            // }
            // System.out.print(hasNonMatchingParent);
            // if (hasNonMatchingParent) {
            //     if((parent_input.equals(""))){
            //     // System.out.print("hiiiiiiiiiiiiiiiiiiiiiiiiii");
            //     hasNonMatchingParent = false;
            //     // yeastRepository.save(yeast);
            //     Yeast createyeast =  yeastRepository.save(yeast);
            //     return createyeast;

            // }
            // else{
            //     throw new InvalidYeastException("Parent input is empty. Cannot save yeast.");
            //     // return existingYeast;
            // }
            // }

            //     else{
            //         Yeast createyeast =  yeastRepository.save(yeast);
            //         return createyeast;
            //         // throw new InvalidYeastException("Parent input is empty. Cannot save yeast.");
            //     }
            
            yeastRepository.save(yeast);
            return yeast;
        
        }finally{
            ChemLabConsts.yeastCreateLock.unlock();
        }
    }

    private void checkParentNameExist(String parentName, String linkToInputFieldName) throws Exception{
        List<String> parent_ids_database = yeastRepository.search_parentId();
        boolean isParentNameOneFound = parent_ids_database.stream().anyMatch(pid -> pid.equals(parentName));
        if(!isParentNameOneFound){
            throw new InvalidYeastException(String.format("Parent name: %s is not exist.", linkToInputFieldName));
        }
    }


            //@Transactional
    @Override
    public List<Yeast> createBatchYeast(MultipartFile file, Users user) throws Exception{
        
        //prevent concurrent access create primers and create batch primers
        ChemLabConsts.yeastCreateLock.lock();
        
        try{
            if(!file.isEmpty()){
                
                //upload csv file
                String filename = fileUploadService.saveFile(file, "csv");
                

                 //csv checking
                 StringBuffer outputReport = perlValidationHandlerService.dataValidation(CsvDataType.YEASTS.getValue(), filename);
                 //System.out.println("output: " + outputReport.toString());
 
                 if(outputReport != null){
                     throw new Exception(outputReport.toString());
                 }

                //csv checking
                List<YeastCsvMapper> csvHandleRecord = csvHandlerService.readYeastCsv(filename);

                List<Yeast> yeastList = new ArrayList<Yeast>();
                
                //get the list of primers lab id
                List<String> yeastLabIDList = generateLabIDService.getYeastLabIDList(csvHandleRecord.size());
                
                //get the list of primers personal id
                List<String> yeastPersonalIDList = generatePersonalIDService.getYeastPersonalID(csvHandleRecord.size(), user.getId(), user.getInitials());

                Date currentDate = new Date();
                // Create a Timestamp from the current date
                Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
                
                
                for(int i=0; i<csvHandleRecord.size(); i++){
                    
                    Yeast yeast = new Yeast();
                    yeast.setUser(user);
                    yeast.setLab_id(yeastLabIDList.get(i));
                    yeast.setPersonal_id(yeastPersonalIDList.get(i));
                    yeast.setGenotype(csvHandleRecord.get(i).getGenotype());
                    yeast.setOther_names(csvHandleRecord.get(i).getOther_names());
                    yeast.setPlasmid(csvHandleRecord.get(i).getPlasmid());
                    yeast.setPlasmid_type(csvHandleRecord.get(i).getPlasmid_type());
                    yeast.setOther_mating_type(csvHandleRecord.get(i).getOther_mating_type());
                    yeast.setParent_name1(csvHandleRecord.get(i).getParent_name1());
                    yeast.setParent_name2(csvHandleRecord.get(i).getParent_name2());
                    yeast.setMarkers_list(csvHandleRecord.get(i).getMarkers_list());
                    yeast.setLocation(csvHandleRecord.get(i).getLocation());
                    yeast.setComments(csvHandleRecord.get(i).getComments());
                    yeast.setDate(currentTimestamp);
                    
                    if(csvHandleRecord.get(i).getMating_type_id()!=null){
                        Mating_types mating_types = new Mating_types();
                        mating_types.setId(csvHandleRecord.get(i).getMating_type_id());
                        yeast.setMating_types(mating_types); 
                    }
                    
                    yeastList.add(yeast);

                }
                
                //save into database
                yeastRepository.saveAll(yeastList);
            }
            
            return null;
        }finally{
            ChemLabConsts.yeastCreateLock.unlock();
        }
    }

}
