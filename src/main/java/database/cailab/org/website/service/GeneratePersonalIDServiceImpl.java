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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mysql.cj.util.StringUtils;

import database.cailab.org.website.repository.BacteriaRepository;
import database.cailab.org.website.repository.MammalianRepository;
import database.cailab.org.website.repository.PlantsRepository;
import database.cailab.org.website.repository.PrimersRepository;
import database.cailab.org.website.repository.YeastRepository;


@Service
public class GeneratePersonalIDServiceImpl implements GeneratePersonalIDService {

    private final BacteriaRepository bacteriaRepository;
    private final PrimersRepository primersRepository;
    private final YeastRepository yeastRepository;
    private final MammalianRepository mammalianRepository;
    private final PlantsRepository plantsRepository;
    
    public GeneratePersonalIDServiceImpl(BacteriaRepository bacteriaRepository, PrimersRepository primersRepository, YeastRepository yeastRepository, 
            MammalianRepository mammalianRepository, PlantsRepository plantsRepository) {
        this.bacteriaRepository = bacteriaRepository;
        this.primersRepository = primersRepository;
        this.yeastRepository = yeastRepository;
        this.mammalianRepository = mammalianRepository;
        this.plantsRepository = plantsRepository;
    }

    @Override
    public List<String> getBacteriaPersonalID(int numberOfBacteria, Integer userID, String userInitials) throws Exception {
        List<String> bacteriaPersonalIDList = new ArrayList<String>();
        
        String latestRecordPersonalID = bacteriaRepository.latestRecordPersonalID(userID);

        int nextPersonalID = (StringUtils.isNullOrEmpty(latestRecordPersonalID)) ? 1  : ApplicationUtils.ExtraDigitAndPlusOne(latestRecordPersonalID);
        
        while(bacteriaRepository.countByPersonalID(nextPersonalID, userID) > 0){
            nextPersonalID++;
        }
        
        for(int i=0; i<numberOfBacteria; i++){
            bacteriaPersonalIDList.add(userInitials + ChemLabConsts.BACTERIA_PREFIX + String.format("%03d", nextPersonalID));
            nextPersonalID++;
        }

        return bacteriaPersonalIDList;
    }

    @Override
    public List<String> getYeastPersonalID(int numberOfYeast, Integer userID, String userInitials) throws Exception {
       List<String> yeastPersonalIDList = new ArrayList<String>(); 

       String latestRecordPersonalID = yeastRepository.latestRecordPersonalID(userID);
       int nextPersonalID = (StringUtils.isNullOrEmpty(latestRecordPersonalID)) ? 1  : ApplicationUtils.ExtraDigitAndPlusOne(latestRecordPersonalID);

       while(yeastRepository.countByPersonalID(nextPersonalID, userID) > 0){
            nextPersonalID++;
        }

        for(int i=0; i<numberOfYeast; i++){
            yeastPersonalIDList.add(userInitials + ChemLabConsts.YEAST_PREFIX + String.format("%03d", nextPersonalID));
            nextPersonalID++;
        }

        return yeastPersonalIDList;
    }

    @Override
    public List<String> getPrimersPersonalID(int numberOfPrimers, Integer userID, String userInitials) throws Exception {
        List<String> primersPersonalIDList = new ArrayList<String>();  
      
        String latestRecordPersonalID = primersRepository.latestRecordPersonalID(userID);
        int nextPersonalID = (StringUtils.isNullOrEmpty(latestRecordPersonalID)) ? 1  : ApplicationUtils.ExtraDigitAndPlusOne(latestRecordPersonalID);      

        while(primersRepository.countByPersonalID(nextPersonalID, userID) > 0){
            nextPersonalID++;
        }

        for(int i=0; i<numberOfPrimers; i++){
            primersPersonalIDList.add(userInitials + ChemLabConsts.PRIMERS_PREFIX + String.format("%03d", nextPersonalID));
            nextPersonalID++;
        }

        return primersPersonalIDList;
    }

    @Override
    public List<String> getMammalianPersonalID(int numberOfMammalian, Integer userID, String userInitials) throws Exception {
        List<String> mammalianPersonalIDList = new ArrayList<String>();  
      
        String latestRecordPersonalID = mammalianRepository.latestRecordPersonalID(userID);
        int nextPersonalID = (StringUtils.isNullOrEmpty(latestRecordPersonalID)) ? 1  : ApplicationUtils.ExtraDigitAndPlusOne(latestRecordPersonalID);      

        while(mammalianRepository.countByPersonalID(nextPersonalID, userID) > 0){
            nextPersonalID++;
        }

        for(int i=0; i<numberOfMammalian; i++){
            mammalianPersonalIDList.add(userInitials + ChemLabConsts.MAMMALIAN_PREFIX + String.format("%03d", nextPersonalID));
            nextPersonalID++;
        }

        return mammalianPersonalIDList;
    }

    @Override
    public List<String> getPlantPersonalID(int numberOfPlants, Integer userID, String userInitials) throws Exception {
        List<String> plantPersonalIDList = new ArrayList<String>();  
      
        String latestRecordPersonalID = plantsRepository.latestRecordPersonalID(userID);
        int nextPersonalID = (StringUtils.isNullOrEmpty(latestRecordPersonalID)) ? 1  : ApplicationUtils.ExtraDigitAndPlusOne(latestRecordPersonalID);      

        while(plantsRepository.countByPersonalID(nextPersonalID, userID) > 0){
            nextPersonalID++;
        }

        for(int i=0; i<numberOfPlants; i++){
            plantPersonalIDList.add(userInitials + ChemLabConsts.PLANTS_PREFIX + String.format("%03d", nextPersonalID));
            nextPersonalID++;
        }

        return plantPersonalIDList;
    }
}
