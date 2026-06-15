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

import java.util.List;

import org.springframework.stereotype.Controller;

import database.cailab.org.website.dto.UserMammalianCountDto;
import database.cailab.org.website.entity.Mammalian;
import database.cailab.org.website.service.ApplicationUtils;
import database.cailab.org.website.service.ChemLabConsts;
import database.cailab.org.website.service.MammalianService;
import database.cailab.org.website.service.MammalianSpeciesService;
import database.cailab.org.website.entity.MammalianSpecies;
import database.cailab.org.website.entity.Users;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Log4j2
@Controller
public class MammalianController {
    private final MammalianService mammalianService;
    private final MammalianSpeciesService mammalianSpeciesService;
    
    public MammalianController(MammalianService mammalianService, MammalianSpeciesService mammalianSpeciesService) {
        this.mammalianService = mammalianService;
        this.mammalianSpeciesService = mammalianSpeciesService;
    }
    
    // mammalian index page
    @GetMapping("/mammalian")
    public String mammalianIndex(Model model) {
        log.info("Mammalian index page returned");
        model.addAttribute("pagetitle", "Mammalian");

        // Get all mammalian data
        List<Mammalian> mammalian = mammalianService.getAllMammaliansOrderByIdDesc();
        model.addAttribute("mammalians", mammalian);

        // Get User and their total mammalians
        List<UserMammalianCountDto> userMammalianCountDto = mammalianService.getUserWithMammalianCount();
        model.addAttribute("userMammalianCountDto", userMammalianCountDto);

        return "mammalian/index";
    }

    // Load mammalian species by ID
    @GetMapping("/mammalian/{id}")
    public String showMammalianDetail(@PathVariable ("id") Integer id, Model model) {
        log.info("Mammalian detail page returned for ID: " + id);
         model.addAttribute("pagetitle", "Show Mammalian");

        Mammalian mammalian = mammalianService.getMammalianById(id);

        if (mammalian == null) {
            log.error(String.format("Mammalian with ID %s not found. Return to mammalian listing page", id));
            return "redirect:/mammalian";
        }

        model.addAttribute("mammalian", mammalian);
        return "mammalian/1";

    }


    // Edit mammalian (Form)
    @GetMapping("/mammalian/update/{id}")
    public String updateMammalian(@PathVariable("id") Integer id, Model model, @AuthenticationPrincipal Users user) {
        log.info("Mammalian update form returned for ID: " + id);
        model.addAttribute("pagetitle", "Update Mammalian");

        Mammalian mammalian = mammalianService.getMammalianByIdWithUnescapesHTML(id);

        // no record found
        if (mammalian == null) {
            log.error(String.format("Mammalian with ID %s not found. Return to mammalian listing page", id));
            return "redirect:/mammalian";
        }

        //user only allow edit his/her own record
        if(user.getId().intValue() != mammalian.getUser().getId().intValue()){
            log.info(String.format("This mammalian record (id: %s) (uid: %s) does not belong to current login user (uid: %s). Edit this record is not allow. Return to listing page", id, mammalian.getUser().getId(), user.getId()));

            return "redirect:/mammalian";
        }

        //convert <br> back to new line (\r\n or \n)
        mammalian.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(mammalian.getComments()));
        model.addAttribute("mammalian", mammalian);

        // Get mammalian species list for dropdown
        List<MammalianSpecies> mammalianSpeciesList = mammalianSpeciesService.getAllMammalianSpecies();
        model.addAttribute("mammalianSpeciesList", mammalianSpeciesList);

        return "mammalian/update";
    }

    // handle update mammalian form data
    @PostMapping(value="/mammalian/update/{id}")
    public String updateMammalian(@PathVariable("id") Integer id, @ModelAttribute("mammalian") @Valid Mammalian mammalian, 
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal Users user) {
        
        log.info("update mammalian");

        boolean hasErrors = bindingResult.hasErrors();
        
        if (!hasErrors) {
            try {
                // save updated mammalian
                mammalianService.updateMammalian(id, mammalian);
                log.info("Update mammalian success.");
            } catch (Exception e) {
                log.info(String.format("Update mammalian exception: %s", e.getMessage()));
                hasErrors = true;
                bindingResult.reject("UpdateError", e.getMessage());
            }
        }

        if (hasErrors) {
            // validate fail

            // Get mammalian species list for dropdown
            List<MammalianSpecies> mammalianSpeciesList = mammalianSpeciesService.getAllMammalianSpecies();
            model.addAttribute("mammalianSpeciesList", mammalianSpeciesList);

            //convert <br> back to new line (\r\n or \n)
            mammalian.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(mammalian.getComments()));

            return "mammalian/update";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "update success.");

            return "redirect:/mammalian/" + id;
        }

    }


    //batch create
    @GetMapping("/mammalian/createBatch")
    public String createBatchMammalian(Model model) {
        log.info("Mammalian batch create form returned");
        model.addAttribute("pagetitle", "Create Mammalian");
        return "mammalian/createbatch";
    }

    // handle batch create mammalian form data
    @PostMapping(value="/mammalian/createBatch")
    public String createBatchMammalian(@RequestParam("fileInput") MultipartFile file, Model model, @AuthenticationPrincipal Users user, RedirectAttributes redirectAttributes) {
        log.info("create batch mammalian");

        boolean hasErrors = false;
        
        try {
            // save new mammalians
            mammalianService.createBatchMammalian(file, user);
            log.info("Create batch mammalian success.");
        } catch (Exception e) {
            hasErrors = true;
            log.info(String.format("Create batch mammalian exception: %s", e.getMessage()));
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

         if(hasErrors){
            return "redirect:/mammalian/createBatch";
         }else{
            return "redirect:/mammalian";
         }
    }


    // Create new mammalian (Form)
    @GetMapping("/mammalian/create")
    public String createMammalian(Model model) {
        log.info("Mammalian form returned");
         model.addAttribute("pagetitle", "Create Mammalian");

        Mammalian mammalian = new Mammalian();
        model.addAttribute("mammalian", mammalian);

        // Get mammalian species list for dropdown
        List<MammalianSpecies> mammalianSpeciesList = mammalianSpeciesService.getAllMammalianSpecies();
        model.addAttribute("mammalianSpeciesList", mammalianSpeciesList);

        return "mammalian/create";
    }

    // handle create new mammalian form data
    @PostMapping(value="/mammalian/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postMethodName(@ModelAttribute("mammalian") @Valid Mammalian mammalian, BindingResult bindingResult, 
                                Model model, RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal Users user) {
        log.info("create mammalian");

        boolean hasErrors = bindingResult.hasErrors();
        
        if (!hasErrors) {
            try {
                // save new mammalian                
                mammalianService.createMammalian(mammalian, user);
                log.info("Create mammalian success.");
            } catch (Exception e) {
                hasErrors = true;
                log.info(String.format("Create mammalian exception: %s", e.getMessage()));
                bindingResult.reject("CreateError", e.getMessage());
            }
        }

        if (hasErrors) {
            // validate fail

            // Get mammalian species list for dropdown
            List<MammalianSpecies> mammalianSpeciesList = mammalianSpeciesService.getAllMammalianSpecies();
            model.addAttribute("mammalianSpeciesList", mammalianSpeciesList);

            //convert <br> back to new line (\r\n or \n)
            mammalian.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(mammalian.getComments()));

            return "mammalian/create";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "create success.");

            return "redirect:/mammalian";
        }
    }
    
}
