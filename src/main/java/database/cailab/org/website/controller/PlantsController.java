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

package database.cailab.org.website.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.extern.log4j.Log4j2;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;

import database.cailab.org.website.service.PlantsService;
import jakarta.validation.Valid;
import database.cailab.org.website.service.ApplicationUtils;
import database.cailab.org.website.service.ChemLabConsts;
import database.cailab.org.website.service.PlantSpeciesService;

import database.cailab.org.website.entity.Plants;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.entity.PlantSpecies;
import database.cailab.org.website.dto.UserPlantsCountDto;

@Log4j2
@Controller
public class PlantsController {
    private final PlantsService plantsService;
    private final PlantSpeciesService plantSpeciesService;


    public PlantsController(PlantsService plantsService, PlantSpeciesService plantSpeciesService) {
        this.plantsService = plantsService;
        this.plantSpeciesService = plantSpeciesService;

    }

    // Plants index page
    @GetMapping("/plants")
    public String plantsIndex(Model model) {
        log.info("Plants index page returned");
        model.addAttribute("pagetitle", "Plants");

        // Get all plants
        List<Plants> plants = plantsService.getAllPlantsOrderByIdDesc();
        model.addAttribute("plants", plants);

        // Get User and their total plants count
        List<UserPlantsCountDto> userPlantsCountDto = plantsService.getUserWithPlantsCount();
        model.addAttribute("userPlantsCountDto", userPlantsCountDto);

        return "plants/index";
    }


    // Load plants species by ID
    @GetMapping("/plants/{id}")
    public String showPlantsDetail(@PathVariable("id") Integer id, Model model) {
        log.info("Plants detail page returned for ID: " + id);
         model.addAttribute("pagetitle", "Show Plants");

        Plants plants = plantsService.getPlantsById(id);

        if (plants == null) {
            log.error(String.format("Plants with ID %s not found. Return to plants listing page", id));
            return "redirect:/plants";
        }

        model.addAttribute("plants", plants);
        return "plants/1";
    }


    // Edit plants (form)
    @GetMapping("/plants/update/{id}")
    public String updatePlants(@PathVariable("id") Integer id, Model model, @AuthenticationPrincipal Users user){
        log.info("Plants update form returned for ID: " + id);
        model.addAttribute("pagetitle", "Update Plants");

        Plants plants = plantsService.getPlantsByIdWithUnescapesHTML(id);

        // no record found
        if (plants == null) {
            log.error(String.format("Plants with ID %s not found. Return to plants listing page", id));
            return "redirect:/plants";
        }

        //user only allow edit his/her own record
        if(user.getId().intValue() != plants.getUser().getId().intValue()){
            log.error(String.format("This plants record (id: %s) (uid: %s) does not belong to current login user (uid: %s). Edit this record is not allow. Return to listing page", id, user.getUsername()));
            return "redirect:/plants";
        }

        //convert <br> back to new line (\r\n or \n)
        plants.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(plants.getComments())); // Assuming comments is a field in Plants, set it to null or handle accordingly
        model.addAttribute("plants", plants);

        // Get plants species list for dropdown
        List<PlantSpecies> plantSpeciesList = plantSpeciesService.getAllPlantSpecies();
        model.addAttribute("plantSpeciesList", plantSpeciesList);

        return "plants/update";
    }

    // handle update plants form data
    @PostMapping(value="/plants/update/{id}")
    public String updatePlants(@PathVariable("id") Integer id, @ModelAttribute("plants") @Valid Plants plants, 
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal Users user) {
        log.info("update plants");

        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            try {
                // save update plants
                plantsService.updatePlants(id, plants);
                log.info("Plants updated successfully");
            }catch(Exception e){
                log.info(String.format("Update plants exception: %s", e.getMessage()));
                hasErrors = true;
                bindingResult.reject("UpdateError", e.getMessage());
            }
        }

        if (hasErrors){
            // validate fail

            // Get plant species list for dropdown
            List<PlantSpecies> plantSpeciesList = plantSpeciesService.getAllPlantSpecies();
            model.addAttribute("plantSpeciesList", plantSpeciesList);

            //convert <br> back to new line (\r\n or \n)
            plants.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(plants.getComments())); 
            
            return "plants/update";
        }else{
            redirectAttributes.addFlashAttribute("ok_message", "update success.");
            return "redirect:/plants/" + id;
        }
                            
    }

    //batch create
    @GetMapping("/plants/createBatch")
    public String createBatchPlants(Model model) {
        log.info("Plants batch create form returned");
        model.addAttribute("pagetitle", "Create Plants");


        return "plants/createbatch";
    }

    // handle batch create plant form data
    @PostMapping(value="/plants/createBatch")
    public String createBatchPlants (@RequestParam("fileInput") MultipartFile file, Model model, @AuthenticationPrincipal Users user, RedirectAttributes redirectAttributes) {
        log.info("create batch plants");

        boolean hasErrors = false;

        try { 
            // save new plants
            plantsService.createBatchPlants(file, user);
            log.info("Create batch plants success.");
        } catch(Exception e){
            hasErrors = true;
            log.info(String.format("Create batch plants exception: %s", e.getMessage()));
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        if(hasErrors){
            return "redirect:/plants/createBatch";
         }else{
            return "redirect:/plants";
         }
    }

    // Create new plants (form)
    @GetMapping("/plants/create")
    public String createPlants(Model model) {
        log.info("Create plants page returned");
        model.addAttribute("pagetitle", "Create Plants");

        
        Plants plants = new Plants();
        model.addAttribute("plants", plants);

        // Get plant species list for dropdown
        List<PlantSpecies> plantSpeciesList = plantSpeciesService.getAllPlantSpecies();
        model.addAttribute("plantSpeciesList", plantSpeciesList);
        
        return "plants/create";
    }

    // handle create new plants form data
    @PostMapping(value="/plants/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createPlants(@ModelAttribute("plants") @Valid Plants plants, BindingResult bindingResult,  
                               Model model, RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal Users user) {
        log.info("Create plants");                                

        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors){
            try{
                plantsService.createPlants(plants, user);
                log.info("Plants created successfully");
            }catch (Exception e) {
                hasErrors = true;
                log.info(String.format("Create plants exception: %s", e.getMessage()));
                bindingResult.reject("CreateError", e.getMessage());
            }
        }


        if (hasErrors) {
            // validate fail

            // Get plant species list for dropdown
            List<PlantSpecies> plantSpeciesList = plantSpeciesService.getAllPlantSpecies();
            model.addAttribute("plantSpeciesList", plantSpeciesList);

            //convert <br> back to new line (\r\n or \n)
            plants.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(plants.getComments())); // Assuming comments is a field in Plants, set it to null or handle accordingly

            return "plants/create";
        }else {
            redirectAttributes.addFlashAttribute("ok_message", "create success.");

            return "redirect:/plants";
        }
    }
    
}
