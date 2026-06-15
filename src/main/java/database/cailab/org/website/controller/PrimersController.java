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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import database.cailab.org.website.dto.UserPrimersCountDto;
import database.cailab.org.website.entity.Orientations;
import database.cailab.org.website.entity.Primers;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.service.ApplicationUtils;
import database.cailab.org.website.service.OrientationsService;
import database.cailab.org.website.service.PrimersService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class PrimersController {

        private final PrimersService primersService;
        private final OrientationsService orientationsService;

    @Autowired
    public PrimersController(PrimersService primersService, OrientationsService orientationsService) {
        this.primersService = primersService;
        this.orientationsService = orientationsService;
    }

    // get mapping for the primers's index page
    @GetMapping("/primers")
    public String primers(Model model) {
        log.info("primers return");
        model.addAttribute("pagetitle", "Primers");

        // get all primers data
        List<Primers> primers = primersService.getAllPrimersOrderByIdDesc();
        model.addAttribute("primers", primers);

        // get User and their total primers
        List<UserPrimersCountDto> userPrimersCountDto = primersService.getUserWithPrimersCount();
        model.addAttribute("userPrimersCountDto", userPrimersCountDto);

        return "primers/index";
    }





        // create new primers
    @GetMapping("/primers/createP")
    public String Create_primers(Model model) {
        log.info("create primers return");
        model.addAttribute("pagetitle", "Create Primers");

        Primers primers = new Primers();
        model.addAttribute("primers", primers);

        List<Orientations> orientations = orientationsService.getAllOrientations();
        model.addAttribute("orientations", orientations);

        return "primers/createP";
    }

    // handle create new primers form data
    @PostMapping(value = "/primers/createP", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Create_primers(@ModelAttribute("primers") @Valid Primers primers,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Users user) {
        log.info("create primers");

        boolean hasErrors = bindingResult.hasErrors();

        

        if (!hasErrors) {
            // save new primers
            try {
                primersService.createPrimers(primers, user);
            } catch (Exception e) {
                hasErrors = true;
                log.info(String.format("Create primers exception: %s", e.getMessage()));
                bindingResult.reject("CreateError", e.getMessage());
            }
        }

        if (hasErrors) {
            List<Orientations> orientations = orientationsService.getAllOrientations();
            model.addAttribute("orientations", orientations);

            //convert <br> back to new line (\r\n or \n)
            primers.setDescription(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getDescription()));
            primers.setSequence(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getSequence()));
            primers.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getComments()));

            return "primers/createP";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "create success.");

            return "redirect:/primers";
        }

    }


    //batch create primers
    @GetMapping("/primers/createBP")
    public String Create_Batch_Primers(Model model){
        log.info("create primers return");
        model.addAttribute("pagetitle", "Create Primers");

        return "primers/createBP";
    }
    
    //batch create primers post
    @PostMapping("/primers/createBP")
    public String Create_Batch_Primers(@RequestParam("fileInput") MultipartFile file, Model model, @AuthenticationPrincipal Users user, RedirectAttributes redirectAttributes){
        boolean hasErrors = false;
        
        log.info("batch create primers");
         
         try{
            primersService.createBatchPrimers(file, user);
         }catch(Exception e){
            hasErrors = true;
            
            log.info(String.format("Batch create Primers exception: %s", e.getMessage()));
            redirectAttributes.addFlashAttribute("error", e.getMessage());
         }
        
         if(hasErrors){
            return "redirect:/primers/createBP";
         }else{
            return "redirect:/primers";
         }
        
    }




    @GetMapping("/primers/{id}")
    public String Show_primers(@PathVariable Integer id, Model model) {
        log.info("show primers return");
        model.addAttribute("pagetitle", "Show Primers");

        Primers primers = primersService.getPrimersById(id);

        // no record found
        if (primers == null) {
            log.info(String.format("No primers record (id: %s) found, return to primers listing page", id));
            return "redirect:/primers";
        }

        model.addAttribute("primers", primers);
        return "primers/1";
    }


    // get mapping for the primers's index page
    @GetMapping("/primers/viewP")
    public String View_primers(Model model) {
        log.info("view primers return");
        model.addAttribute("pagetitle", "View Primers");
        return "primers/viewP";
    }



        // edit primers record
    @GetMapping("/primers/updateP/{id}")
    public String Update_primers(@PathVariable Integer id, Model model, @AuthenticationPrincipal Users user) {
        log.info("update primers return");
        model.addAttribute("pagetitle", "Update primers");

        Primers primers = primersService.getPrimersByIdWithUnescapesHTML(id);
        
        //no record found
        if(primers == null){
            log.info(String.format("No primers record (id: %s) found, return to primers listing page", id));
            
            return "redirect:/primers";
        }

        //user only allow edit his/her own record
        if(user.getId().intValue() != primers.getUser().getId().intValue()){
            log.info(String.format("This primers record (id: %s) (uid: %s) does not belong to this login user (uid: %s), cannot edit and return to primers listing page", id, primers.getUser().getId(), user.getId()));

            return "redirect:/primers";
        }
        
        //convert <br> back to new line (\r\n or \n)
        primers.setDescription(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getDescription()));
        primers.setSequence(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getSequence()));
        primers.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getComments()));

        model.addAttribute("primers", primers);

        List<Orientations> orientations = orientationsService.getAllOrientations();
        model.addAttribute("orientations", orientations);
        return "primers/updateP";
    }


    @PostMapping(value = "/primers/updateP/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Update_primers(@PathVariable("id") Integer id, @ModelAttribute("primers") @Valid Primers primers,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        log.info("update primers");
        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            // when it pass the vaildation, then try to do update.
            try {
                primersService.updatePrimers(id, primers);
            } catch (Exception e) {
                log.info(String.format("Update primers exception: %s", e.getMessage()));
                hasErrors = true;
            }
        }
        // either vaildation error or update error
        if (hasErrors) {
            // validate fail
            List<Orientations> orientations = orientationsService.getAllOrientations();
            model.addAttribute("orientations", orientations);

            //convert <br> back to new line (\r\n or \n)
            primers.setDescription(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getDescription()));
            primers.setSequence(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getSequence()));
            primers.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(primers.getComments()));
            
            return "primers/updateP";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "Update success.");

            return "redirect:/primers/" + id;
        }

    }

}
