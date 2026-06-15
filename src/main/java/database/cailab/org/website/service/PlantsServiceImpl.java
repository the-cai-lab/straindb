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

import database.cailab.org.website.repository.PlantsRepository;
import jakarta.persistence.EntityNotFoundException;
import database.cailab.org.website.repository.PlantSpeciesRepository;
import database.cailab.org.website.entity.PlantSpecies;
import database.cailab.org.website.entity.Plants;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.csvmapper.PlantsCsvMapper;
import database.cailab.org.website.dto.UserPlantsCountDto;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;



@Service
public class PlantsServiceImpl implements PlantsService {
    private final GenerateLabIDService generateLabIDService;
    private final GeneratePersonalIDService generatePersonalIDService;
    private final PlantsRepository plantsRepository;
    private final PlantSpeciesRepository plantSpeciesRepository;
    private final FileUploadService fileUploadService;
    private final PerlValidationHandlerService perlValidationHandlerService;
    private final CsvHandlerService csvHandlerService;


    public PlantsServiceImpl(PlantsRepository plantsRepository, PlantSpeciesRepository plantSpeciesRepository, 
                             GenerateLabIDService generateLabIDService, GeneratePersonalIDService generatePersonalIDService,
                             FileUploadService fileUploadService, PerlValidationHandlerService perlValidationHandlerService,
                             CsvHandlerService csvHandlerService) {
        this.plantsRepository = plantsRepository;
        this.plantSpeciesRepository = plantSpeciesRepository;
        this.generateLabIDService = generateLabIDService;
        this.generatePersonalIDService = generatePersonalIDService;
        this.fileUploadService = fileUploadService;
        this.perlValidationHandlerService = perlValidationHandlerService;
        this.csvHandlerService = csvHandlerService;
    }

    @Override
    public List<Plants> getAllPlantsOrderByIdDesc() {
        return plantsRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<UserPlantsCountDto> getUserWithPlantsCount() {
        return plantsRepository.findPlantsCountByUserOrderByName();
    }

    @Override
    public Plants getPlantsById(Integer id) {
        return plantsRepository.findById(id).orElse(null);
    }

    @Override
    public Plants createPlants(Plants plants, Users user) throws Exception {
        // prevent concurrent creation of plants via different methods (single record creation or batch creation)
        ChemLabConsts.plantsCreateLock.lock();

        try {
            int numberOfPlantsIDRequested = 1;
            int retrieveFirstRecord = 0; // we only need the first record from the list of plant IDs generated

            plants.setUser(user);
            plants.setLab_id(generateLabIDService.getPlantsLabIDList(numberOfPlantsIDRequested).get(retrieveFirstRecord));
            plants.setPersonal_id(generatePersonalIDService.getPlantPersonalID(numberOfPlantsIDRequested, user.getId(), user.getInitials()).get(retrieveFirstRecord));

            if (plants.getPlantSpecies().getNcbiid() == null) {
                throw new Exception("Plant species is required.");
            }

            // Set the plants species based on the selected NCBI ID
            Optional<PlantSpecies> plantSpecies = plantSpeciesRepository.findByNcbiid(plants.getPlantSpecies().getNcbiid());
            if (plantSpecies.isPresent()) {
                plants.setPlantSpecies(plantSpecies.get());
            } else {
                throw new Exception("Plants species with NCBI ID " + plants.getPlantSpecies().getNcbiid() + " not found.");
            }

            // save the plants record to the database
            plantsRepository.save(plants);

            return plants;
        } finally {
            ChemLabConsts.plantsCreateLock.unlock();
        }
    }
    
    @Override
    public Plants getPlantsByIdWithUnescapesHTML(Integer id) {
        Plants plants = getPlantsById(id);

        if (plants != null) {
            // Comment
            // may contain HTML entities that need to be unescaped
            plants.setComments(StringEscapeUtils.unescapeHtml4(plants.getComments()));
        }

        return plants;
    }

    @Override
    public Plants updatePlants(Integer id, Plants plants) throws Exception {
        // You can find some default value at Plants Entity

        // check for existing record
        Plants existingPlants = plantsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
            String.format("Unable to update because there is no matching plants record found (id: %s) ", id)));

        // map of the ID
        plants.setId(id);

        // Submitted form data doesn't contains all required data, so we need to set the missing data from the existing record 
        plants.setUser(existingPlants.getUser());
        plants.setCreated_at(existingPlants.getCreated_at());
        plants.setLab_id(existingPlants.getLab_id());
        plants.setPersonal_id(existingPlants.getPersonal_id());

        // Set the plants species based on the selected NCBI ID
        Optional<PlantSpecies> plantSpecies = plantSpeciesRepository.findByNcbiid(plants.getPlantSpecies().getNcbiid());
        if (plantSpecies.isPresent()) {
            plants.setPlantSpecies(plantSpecies.get());
        } else {
            throw new Exception("Plants species with NCBI ID " + plants.getPlantSpecies().getNcbiid() + " not found.");
        }
        
        // save the updated plants record to the database
        return plantsRepository.save(plants);
    }

    @Override
    public List<Plants> createBatchPlants(MultipartFile file, Users user) throws Exception{
        // prevent concurrent creation of plant via different methods (single record creation or batch creation)
        ChemLabConsts.plantsCreateLock.lock();

        try { 
            if(!file.isEmpty()) {
                // upload csv file
                String filename = fileUploadService.saveFile(file, "csv");  
                
                // validate csv data
                StringBuffer outputReport = perlValidationHandlerService.dataValidation(CsvDataType.PLANTS.getValue(), filename);

                if(outputReport != null){
                    throw new Exception(outputReport.toString());
                }

                List<PlantsCsvMapper> csvHandleRecord = csvHandlerService.readPlantsCsv(filename);

                List<Plants> plantsList = new ArrayList<>();

                // Generate lab ID and personal ID for each plant record
                List<String> plantsLabIDList = generateLabIDService.getPlantsLabIDList(csvHandleRecord.size());

                List<String> plantsPersonalIDList = generatePersonalIDService.getPlantPersonalID(csvHandleRecord.size(), user.getId(), user.getInitials());

                Timestamp currentTimestamp = ApplicationUtils.getCurrentTimestamp();

                // Create index counter for lab ID and personal ID
                AtomicInteger index = new AtomicInteger();

                csvHandleRecord.stream().forEach(csvRecord -> {
                    int currentIndex = index.getAndIncrement(); // Get the current index for lab ID and personal ID
                    
                    Plants plants = new Plants();
                    plants.setUser(user);
                    plants.setPersonal_id(plantsPersonalIDList.get(currentIndex));
                    plants.setLab_id(plantsLabIDList.get(currentIndex));
                    plants.setName(csvRecord.getName());
                    plants.setGrowthConditions(csvRecord.getGrowthConditions());
                    plants.setGenotype(csvRecord.getGenotype());
                    plants.setSource(csvRecord.getSource());
                    plants.setMarker(csvRecord.getMarker());
                    plants.setMedia(csvRecord.getMedia());

                    // Set the plants species based on the selected NCBI ID
                    Optional<PlantSpecies> plantSpecies = plantSpeciesRepository.findByNcbiid(csvRecord.getPlantSpecies());
                    if(plantSpecies.isPresent()) {
                        plants.setPlantSpecies(plantSpecies.get());
                    } else {
                        throw new RuntimeException("Plants species with NCBI ID " + csvRecord.getPlantSpecies() + " not found.");
                    }

                    plants.setComments(csvRecord.getComments());
                    plants.setLocation(csvRecord.getLocation());
                    plants.setDate(currentTimestamp);

                    plantsList.add(plants);
                });

                // save all plants records to the database
                plantsRepository.saveAll(plantsList);
            }

            return null;
        } finally {
            ChemLabConsts.plantsCreateLock.unlock();
        }
    }
}
