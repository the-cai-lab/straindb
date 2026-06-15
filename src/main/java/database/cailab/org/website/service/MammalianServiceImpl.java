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


import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.multipart.MultipartFile;

import database.cailab.org.website.entity.Mammalian;
import database.cailab.org.website.entity.MammalianSpecies;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.repository.MammalianRepository;
import database.cailab.org.website.repository.MammalianSpeciesRepository;
import jakarta.persistence.EntityNotFoundException;
import database.cailab.org.website.csvmapper.MammalianCsvMapper;
import database.cailab.org.website.dto.UserMammalianCountDto;

@Service
public class MammalianServiceImpl implements MammalianService {

    private final MammalianRepository mammalianRepository;
    private final MammalianSpeciesRepository mammalianSpeciesRepository;
    private final GenerateLabIDService generateLabIDService;
    private final GeneratePersonalIDService generatePersonalIDService;
    private final FileUploadService fileUploadService;
    private final PerlValidationHandlerService perlValidationHandlerService;
    private final CsvHandlerService csvHandlerService;

    public MammalianServiceImpl(MammalianRepository mammalianRepository, MammalianSpeciesRepository mammalianSpeciesRepository, GenerateLabIDService generateLabIDService, GeneratePersonalIDService generatePersonalIDService, FileUploadService fileUploadService, PerlValidationHandlerService perlValidationHandlerService, CsvHandlerService csvHandlerService) {
        this.mammalianRepository = mammalianRepository;
        this.mammalianSpeciesRepository = mammalianSpeciesRepository;
        this.generateLabIDService = generateLabIDService;
        this.generatePersonalIDService = generatePersonalIDService;
        this.fileUploadService = fileUploadService;
        this.perlValidationHandlerService = perlValidationHandlerService;
        this.csvHandlerService = csvHandlerService;
    }

    @Override
    public List<Mammalian> getAllMammaliansOrderByIdDesc() {
        return mammalianRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<UserMammalianCountDto> getUserWithMammalianCount() {
        return mammalianRepository.findMammalianCountByUserOrderByName();
    }

    @Override
    public Mammalian getMammalianById(Integer id) {
        return mammalianRepository.findById(id).orElse(null); 
    }

    @Override
    public Mammalian createMammalian(Mammalian mammalian, Users user) throws Exception {
        // prevent concurrent creation of mammalian via different methods (single record creation or batch creation)
        ChemLabConsts.mammalianCreateLock.lock();

        try{
            int numberOfMammalianIDRquested = 1; //requesting one mammalian ID here
            int retrieveFirstRecord = 0; // we only need the first record from the list of mammalian IDs generated

            mammalian.setUser(user);
            mammalian.setLab_id(generateLabIDService.getMammalianLabIDList(numberOfMammalianIDRquested).get(retrieveFirstRecord));
            mammalian.setPersonal_id(generatePersonalIDService.getMammalianPersonalID(numberOfMammalianIDRquested, user.getId(), user.getInitials()).get(retrieveFirstRecord));

            if(mammalian.getMammalianSpecies().getNcbiid() == null) {
                throw new Exception("Mammalian species is required.");
            }
            
            // Set the mammalian species based on the selected NCBI ID
            Optional<MammalianSpecies> mammalianSpecies = mammalianSpeciesRepository.findByNcbiid(mammalian.getMammalianSpecies().getNcbiid());
            if (mammalianSpecies.isPresent()) {
                mammalian.setMammalianSpecies(mammalianSpecies.get());
            }else {
                throw new Exception("Mammalian species with NCBI ID " + mammalian.getMammalianSpecies().getNcbiid() + " not found.");
            }

            // save the mammalian record
            mammalianRepository.save(mammalian);
            return mammalian;

        }finally {
            // release the lock
            ChemLabConsts.mammalianCreateLock.unlock();
        }
    }

    @Override
    public Mammalian getMammalianByIdWithUnescapesHTML(Integer id) {
        Mammalian mammalian = getMammalianById(id);
        if (mammalian != null) {
            // Comment
            // may contain HTML entities that need to be unescaped
            mammalian.setComments(StringEscapeUtils.unescapeHtml4(mammalian.getComments()));
        }
        return mammalian;
    }

    @Override
    public Mammalian updateMammalian(Integer id, Mammalian mammalian) throws Exception {
        // You can find some default value at Mammalian Entity

        // check for existing record
        Mammalian existingMammalian = mammalianRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to update because there is no matching mammalian record found (id: %s) ", id)));
        
        // map of the ID
        mammalian.setId(id);

        // Submitted form data doesn't contains all required data, so we need to set the missing data from the existing record 
        mammalian.setUser(existingMammalian.getUser());
        // bacteria.setDate(existingBacteria.getDate());
        mammalian.setCreated_at(existingMammalian.getCreated_at());
        mammalian.setLab_id(existingMammalian.getLab_id());
        mammalian.setPersonal_id(existingMammalian.getPersonal_id());

        // Set the mammalian species based on the selected NCBI ID
        Optional<MammalianSpecies> mammalianSpecies = mammalianSpeciesRepository.findByNcbiid(mammalian.getMammalianSpecies().getNcbiid());
        if (mammalianSpecies.isPresent()) {
            mammalian.setMammalianSpecies(mammalianSpecies.get());
        }else {
            throw new Exception("Mammalian species with NCBI ID " + mammalian.getMammalianSpecies().getNcbiid() + " not found.");
        }
        
        // save the mammalian record
        return mammalianRepository.save(mammalian);

    }
    
    @Override
    public List<Mammalian> createBatchMammalian(MultipartFile file, Users user) throws Exception {
        // prevent concurrent creation of mammalian via different methods (single record creation or batch creation)
        ChemLabConsts.mammalianCreateLock.lock();
        try {
            if(!file.isEmpty()){
                // upload csv file
                String filename = fileUploadService.saveFile(file, "csv");

                // validate csv data
                StringBuffer outputReport = perlValidationHandlerService.dataValidation(CsvDataType.MAMMALIAN.getValue(), filename);

                if(outputReport != null){
                    throw new Exception(outputReport.toString());
                }

                List<MammalianCsvMapper> csvHandleRecord = csvHandlerService.readMammalianCsv(filename);

                List<Mammalian> mammaliansList = new ArrayList<Mammalian>();

                // Generate lab ID and personal ID for each mammalian record
                List<String> mammaliansLabIDList = generateLabIDService.getMammalianLabIDList(csvHandleRecord.size());
                
                List<String> mammaliansPersonalIDList = generatePersonalIDService.getMammalianPersonalID(csvHandleRecord.size(), user.getId(), user.getInitials());

                Timestamp currentTimestamp = ApplicationUtils.getCurrentTimestamp();

                // Create index counter for lab ID and personal ID
                AtomicInteger index = new AtomicInteger();

                csvHandleRecord.stream().forEach(csvRecord -> {
                    int currentIndex = index.getAndIncrement(); // Get the current index for lab ID and personal ID
                    
                    Mammalian mammalian = new Mammalian();
                    mammalian.setUser(user);
                    mammalian.setPersonal_id(mammaliansPersonalIDList.get(currentIndex));
                    mammalian.setLab_id(mammaliansLabIDList.get(currentIndex));
                    mammalian.setName(csvRecord.getName());
                    mammalian.setCell_line(csvRecord.getCell_line());
                    mammalian.setPassage_number(csvRecord.getPassage_number());
                    mammalian.setCell_type(csvRecord.getCell_type());
                    mammalian.setKaryotype(csvRecord.getKaryotype());
                    mammalian.setGenotype(csvRecord.getGenotype());
                    mammalian.setSource(csvRecord.getSource());
                    mammalian.setMarker(csvRecord.getMarker());
                    mammalian.setMedia(csvRecord.getMedia());

                    // Set the mammalian species based on the selected NCBI ID
                    Optional<MammalianSpecies> mammalianSpecies = mammalianSpeciesRepository.findByNcbiid(csvRecord.getSpecies());
                    if (mammalianSpecies.isPresent()) {
                        mammalian.setMammalianSpecies(mammalianSpecies.get());
                    }else {
                        throw new RuntimeException("Mammalian species with NCBI ID " + csvRecord.getSpecies() + " not found.");
                    }
                    
                    try{
                        mammalian.setDatefrozen(ApplicationUtils.ConvertStringToTimestamp(csvRecord.getDatefrozen())); 
                    }catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    
                    mammalian.setComments(csvRecord.getComments());
                    mammalian.setLocation(csvRecord.getLocation());
                    mammalian.setDate(currentTimestamp);

                    mammaliansList.add(mammalian);
                });

                // save all mammalian records
                mammalianRepository.saveAll(mammaliansList);
            }

            return null;
        }finally {
            // release the lock
            ChemLabConsts.mammalianCreateLock.unlock();
        }
    }
}
