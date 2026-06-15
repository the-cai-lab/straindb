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

import database.cailab.org.website.entity.Bacteria;
import database.cailab.org.website.entity.Mammalian;
import database.cailab.org.website.entity.Plants;
import database.cailab.org.website.entity.Primers;
import database.cailab.org.website.entity.Yeast;
import database.cailab.org.website.repository.BacteriaRepository;
import database.cailab.org.website.repository.PrimersRepository;
import database.cailab.org.website.repository.YeastRepository;
import database.cailab.org.website.repository.MammalianRepository;
import database.cailab.org.website.repository.PlantsRepository;

@Service
public class GenerateLabIDServiceImpl implements GenerateLabIDService {
    private final BacteriaRepository bacteriaRepository;
    private final PrimersRepository primersRepository;
    private final YeastRepository yeastRepository;
    private final MammalianRepository mammalianRepository;
    private final PlantsRepository plantsRepository;


    public GenerateLabIDServiceImpl(BacteriaRepository bacteriaRepository, PrimersRepository primersRepository,
            YeastRepository yeastRepository, MammalianRepository mammalianRepository, PlantsRepository plantsRepository) {
        this.bacteriaRepository = bacteriaRepository;
        this.primersRepository = primersRepository;
        this.yeastRepository = yeastRepository;
        this.mammalianRepository = mammalianRepository;
        this.plantsRepository = plantsRepository;
    }


    //get a group of bacteria lab id for bacteria batch create, you can get 1 bacteria lab id as well
    @Override
    public List<String> getBacteriaLabIDList(int numberOfBacteria) throws Exception {
        List<String> bacteriaLabIDList = new ArrayList<String>();
        
        Bacteria bacteria = bacteriaRepository.findTopByOrderByIdDesc().orElse(null);
        int nextlabID = (bacteria != null) ?   ApplicationUtils.ExtraDigitAndPlusOne(bacteria.getLab_id()) : 1 ;
        
        // Latest record may not own the largest Lab ID because of data quality issue.
        // To ensure the new Lab ID is unique, we need to check existing data. 
        // If there is a duplicate Lab ID, we will increment the nextLabID until it becomes unique.
        while(bacteriaRepository.countByLabID(nextlabID) >0){
            nextlabID++;
        }

        for(int i=0; i<numberOfBacteria; i++){
            bacteriaLabIDList.add(ChemLabConsts.LAB_INITIALS + ChemLabConsts.BACTERIA_PREFIX + String.format("%03d", nextlabID));
            nextlabID++;
        }
        return bacteriaLabIDList;
    }

    @Override
    public List<String> getYeastLabIDList(int numberOfYeast) throws Exception {
        List<String> yeastLabIDList = new ArrayList<String>();
        
        Yeast yeast = yeastRepository.findTopByOrderByIdDesc().orElse(null);
        int nextlabID = (yeast != null) ?   ApplicationUtils.ExtraDigitAndPlusOne(yeast.getLab_id()) : 1 ;
        
        // Latest record may not own the largest Lab ID because of data quality issue.
        // To ensure the new Lab ID is unique, we need to check existing data. 
        // If there is a duplicate Lab ID, we will increment the nextLabID until it becomes unique.
        while(yeastRepository.countByLabID(nextlabID) >0){
            nextlabID++;
        }

        for(int i=0; i<numberOfYeast; i++){
            yeastLabIDList.add(ChemLabConsts.LAB_INITIALS + ChemLabConsts.YEAST_PREFIX + String.format("%03d", nextlabID));
            nextlabID++;
        }
        
        return yeastLabIDList;
    }

        //get a group of yeast lab id for yeast batch create, you can get 1 yeast lab id as well
    @Override
    public List<String> getPrimersLabIDList(int numberOfPrimers) throws Exception {
        List<String> primersLabIDList = new ArrayList<String>();
        
        Primers primers = primersRepository.findTopByOrderByIdDesc().orElse(null);
        int nextlabID = (primers != null) ?   ApplicationUtils.ExtraDigitAndPlusOne(primers.getLab_id()) : 1 ;
        
        // Latest record may not own the largest Lab ID because of data quality issue.
        // To ensure the new Lab ID is unique, we need to check existing data. 
        // If there is a duplicate Lab ID, we will increment the nextLabID until it becomes unique.
        while(primersRepository.countByLabID(nextlabID) >0){
            nextlabID++;
        }

        for(int i=0; i<numberOfPrimers; i++){
            primersLabIDList.add(ChemLabConsts.LAB_INITIALS + ChemLabConsts.PRIMERS_PREFIX + String.format("%03d", nextlabID));
            nextlabID++;
        }
        return primersLabIDList;
    }

    @Override
    public List<String> getMammalianLabIDList(int numberOfMammalian) throws Exception {
        List<String> mammalianLabIDList = new ArrayList<String>();
        
        Mammalian mammalian = mammalianRepository.findTopByOrderByIdDesc().orElse(null);
        int nextlabID = (mammalian != null) ?   ApplicationUtils.ExtraDigitAndPlusOne(mammalian.getLab_id()) : 1 ;
        
        // Latest record may not own the largest Lab ID because of data quality issue.
        // To ensure the new Lab ID is unique, we need to check existing data. 
        // If there is a duplicate Lab ID, we will increment the nextLabID until it becomes unique.
        while(mammalianRepository.countByLabID(Integer.toString(nextlabID)) >0){
            nextlabID++;
        }

        for(int i=0; i<numberOfMammalian; i++){
            mammalianLabIDList.add(ChemLabConsts.LAB_INITIALS + ChemLabConsts.MAMMALIAN_PREFIX + String.format("%03d", nextlabID));
            nextlabID++;
        }

        return mammalianLabIDList;
    }

    @Override
    public List<String> getPlantsLabIDList(int numberOfPlants) throws Exception {
        List<String> plantsLabIDList = new ArrayList<String>();
        
        Plants plants = plantsRepository.findTopByOrderByIdDesc().orElse(null);
        int nextlabID = (plants != null) ?   ApplicationUtils.ExtraDigitAndPlusOne(plants.getLab_id()) : 1 ;
        
        // Latest record may not own the largest Lab ID because of data quality issue.
        // To ensure the new Lab ID is unique, we need to check existing data. 
        // If there is a duplicate Lab ID, we will increment the nextLabID until it becomes unique.
        while(plantsRepository.countByLabID(Integer.toString(nextlabID)) >0){
            nextlabID++;
        }

        for(int i=0; i<numberOfPlants; i++){
            plantsLabIDList.add(ChemLabConsts.LAB_INITIALS + ChemLabConsts.PLANTS_PREFIX + String.format("%03d", nextlabID));
            nextlabID++;
        }
        
        return plantsLabIDList;
    }

}
