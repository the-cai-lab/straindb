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
// import org.springframework.http.MediaType;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import database.cailab.org.website.entity.Bacteria;
import database.cailab.org.website.entity.BacterialMarkers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.dto.UserBacteriaCountDto;
import database.cailab.org.website.service.ApplicationUtils;
import database.cailab.org.website.service.BacteriaService;
import database.cailab.org.website.service.BacterialMarkersService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
// @RequestMapping(value = "/bacteria", produces = { MediaType.TEXT_HTML_VALUE
// })
public class BacteriaController {
    private final BacteriaService bacteriaService;
    private final BacterialMarkersService bacteriaMarkersService;

    @Autowired
    public BacteriaController(BacteriaService bacteriaService,
            BacterialMarkersService bacteriaMarkersService) {
        this.bacteriaService = bacteriaService;
        this.bacteriaMarkersService = bacteriaMarkersService;
    }

    // get mapping for the bacteria's index page
    // Load all bacteria data
    @GetMapping("/bacteria")
    public String bacteria(Model model) {
        log.info("bacteria return");
        model.addAttribute("pagetitle", "Bacteria");

        // get all bacteria data
        List<Bacteria> bacteria = bacteriaService.getAllBacteriaOrderByIdDesc();
        model.addAttribute("bacteria", bacteria);

        // get User and their total bacteria
        List<UserBacteriaCountDto> userBacteriaCountDto = bacteriaService.getUserWithBacteriaCount();
        model.addAttribute("userBacteriaCountDto", userBacteriaCountDto);

        return "bacteria/index";
    }

    // create new bacteria
    @GetMapping("/bacteria/createB")
    public String Create_bacteria(Model model) {
        log.info("create bacteria return");
        model.addAttribute("pagetitle", "Create Bacteria");

        Bacteria bacteria = new Bacteria();
        model.addAttribute("bacteria", bacteria);

        // get bacteria marker list
        List<BacterialMarkers> bacterialMarkers = bacteriaMarkersService.getAllBacteriaMarkers();
        model.addAttribute("bacterialMarkers", bacterialMarkers);

        return "bacteria/createB";
    }

    // handle create new bacteria form data
    @PostMapping(value = "/bacteria/createB", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Create_bacteria(@ModelAttribute("bacteria") @Valid Bacteria bacteria,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Users user) {
        log.info("create bacteria");

        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            // save new bacteria
            try {
                bacteriaService.createBacteria(bacteria, user);
            } catch (Exception e) {
                hasErrors = true;
                // return "bacteria/createB";
                log.info(String.format("Create bacteris exception: %s", e.getMessage()));
                bindingResult.reject("CreateError", e.getMessage());
            }
        }

        if (hasErrors) {
            // validate fail
            // if data can't be display, enable the following code again
            // model.addAttribute("pagetitle", "Update Bacteria");

            // get bacteria marker list
            List<BacterialMarkers> bacterialMarkers = bacteriaMarkersService.getAllBacteriaMarkers();
            model.addAttribute("bacterialMarkers", bacterialMarkers);
            
            //convert <br> back to new line (\r\n or \n)
            bacteria.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(bacteria.getComments()));
            
            return "bacteria/createB";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "create success.");

            return "redirect:/bacteria";
        }

    }


    //batch create bacteria
    @GetMapping("/bacteria/createBB")
    public String Create_Batch_Bacteria(Model model){
        log.info("create bacteria return");
        model.addAttribute("pagetitle", "Create Bacteria");

        return "bacteria/createBB";
    }
    
    //batch create bacteria post
    @PostMapping("/bacteria/createBB")
    public String Create_Batch_Bacteria(@RequestParam("fileInput") MultipartFile file, Model model, @AuthenticationPrincipal Users user, RedirectAttributes redirectAttributes){
        boolean hasErrors = false;
        
        log.info("batch create bacteria");
         
         try{
            bacteriaService.createBatchBacteria(file, user);
         }catch(Exception e){
            hasErrors = true;
            
            log.info(String.format("Batch create bacteria exception: %s", e.getMessage()));
            redirectAttributes.addFlashAttribute("error", e.getMessage());
         }
        
         if(hasErrors){
            return "redirect:/bacteria/createBB";
         }else{
            return "redirect:/bacteria";
         }
        
    }
    
    
    // Load specific bacteria data by ID
    @GetMapping("/bacteria/{id}")
    public String Show_bacteria(@PathVariable Integer id, Model model) {
        log.info("show bacteria return");
        model.addAttribute("pagetitle", "Show Bacteria");

        Bacteria bacteria = bacteriaService.getBacteriaById(id);

        // no record found
        if (bacteria == null) {
            log.info(String.format("No bacteria record (id: %s) found, return to bacteria listing page", id));

            return "redirect:/bacteria";
        }

        model.addAttribute("bacteria", bacteria);
        return "bacteria/1";
    }

    // edit bacteria record
    @GetMapping("/bacteria/updateB/{id}")
    public String Update_bacteria(@PathVariable Integer id, Model model, @AuthenticationPrincipal Users user) {
        log.info("update bacteria return");
        model.addAttribute("pagetitle", "Update Bacteria");

        Bacteria bacteria = bacteriaService.getBacteriaByIdWithUnescapesHTML(id);

        // no record found
        if (bacteria == null) {
            log.info(String.format("No bacteria record (id: %s) found, return to bacteria listing page", id));

            return "redirect:/bacteria";
        }


        //user only allow edit his/her own record
        if(user.getId().intValue() != bacteria.getUser().getId().intValue()){
            log.info(String.format("This bacteria record (id: %s) (uid: %s) does not belong to this login user (uid: %s), cannot edit and return to bacteria listing page", id, bacteria.getUser().getId(), user.getId()));

            return "redirect:/bacteria";
        }
        
        //convert <br> back to new line (\r\n or \n)
        bacteria.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(bacteria.getComments()));
        model.addAttribute("bacteria", bacteria);

        // get bacteria marker list
        List<BacterialMarkers> bacterialMarkers = bacteriaMarkersService.getAllBacteriaMarkers();
        model.addAttribute("bacterialMarkers", bacterialMarkers);

        return "bacteria/updateB";
    }

    // edited bateria form post
    @PostMapping(value = "/bacteria/updateB/{id}")
    public String Update_bacteria(@PathVariable("id") Integer id, @RequestParam("fileInput") MultipartFile file, @ModelAttribute("bacteria") @Valid Bacteria bacteria,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        log.info("update bacteria");

        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            // when it pass the vaildation, then try to do update.
            try {
                bacteriaService.updateBacteria(id, bacteria, file);
            } catch (Exception e) {
                log.info(String.format("Update bacteris exception: %s", e.getMessage()));
                hasErrors = true;
                bindingResult.reject("UpdatgeError", e.getMessage());
            }
        }

        // either vaildation error or update error
        if (hasErrors) {
            // validate fail
            // if data can't be display, enable the following code again
            // model.addAttribute("pagetitle", "Update Bacteria");

            // get bacteria marker list
            List<BacterialMarkers> bacterialMarkers = bacteriaMarkersService.getAllBacteriaMarkers();
            model.addAttribute("bacterialMarkers", bacterialMarkers);

            //convert <br> back to new line (\r\n or \n)
            bacteria.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(bacteria.getComments()));

            return "bacteria/updateB";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "Update success.");

            return "redirect:/bacteria/" + id;
        }

    }

}
