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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import database.cailab.org.website.dto.UserYeastCountDto;
import database.cailab.org.website.entity.Mating_types;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.entity.Yeast;
import database.cailab.org.website.service.ApplicationUtils;
import database.cailab.org.website.service.InvalidYeastException;
import database.cailab.org.website.service.MatingTypesService;
import database.cailab.org.website.service.YeastService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class YeastController {


    private final YeastService yeastService;
    private final MatingTypesService matingtypesService;

    @Autowired
    public YeastController(YeastService yeastService, MatingTypesService matingtypesService) {
        this.yeastService = yeastService;
        this.matingtypesService = matingtypesService;
    }

    // get mapping for the yeast's index page
    @GetMapping("/yeast")
    public String yeast(Model model) {
        log.info("yeast return");
        model.addAttribute("pagetitle", "Yeast");


        // get all yeast data
        List<Yeast> yeast = yeastService.getAllYeastOrderByIdDesc();
        model.addAttribute("yeast", yeast);

        // get User and their total yeast
        List<UserYeastCountDto> userYeastCountDto = yeastService.getUserWithYeastCount();
        model.addAttribute("userYeastCountDto", userYeastCountDto);

        return "yeast/index";
    }
    


            // create new primers
    @GetMapping("/yeast/createY")
    public String Create_yeast(Model model) {
        log.info("create yeast return");
        model.addAttribute("pagetitle", "Create Yeast");

        Yeast yeast = new Yeast();
        model.addAttribute("yeast", yeast);

        List<Mating_types> mating_types = matingtypesService.getAllMatingTypes();
        model.addAttribute("mating_types", mating_types);

        return "yeast/createY";
    }

    // handle create new primers form data
    @PostMapping(value = "/yeast/createY", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Create_yeast(@ModelAttribute("yeast") @Valid Yeast yeast,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Users user) {
        log.info("create yeast");

        boolean hasErrors = bindingResult.hasErrors();

        

        if (!hasErrors) {
            // when it pass the vaildation, then try to do create.
            try {
                yeastService.createYeast(yeast, user);
            }catch (InvalidYeastException e) {
                log.info(String.format("create yeast exception: %s", e.getMessage()));
                hasErrors = true;
                //throw e;
                bindingResult.reject("CreateError", e.getMessage());
            }            
            catch (Exception e) {
                log.info(String.format("create yeast exception: %s", e.getMessage()));
                hasErrors = true;
                bindingResult.reject("CreateError", e.getMessage());
            }
        }

        if (hasErrors) {
            List<Mating_types> mating_types = matingtypesService.getAllMatingTypes();
            model.addAttribute("mating_types", mating_types);
            
            //convert <br> back to new line (\r\n or \n)
            yeast.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(yeast.getComments()));
            
            //     List<Yeast_Markers> yeast_markers = yeastmarkersService.getAllYeastMarkers();
            // model.addAttribute("Yeast_Markers", yeast_markers);
            return "yeast/createY";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "create success.");

            return "redirect:/yeast";
        }

    }



        //batch create yeast
    @GetMapping("/yeast/createBY")
    public String Create_Batch_Yeast(Model model){
        log.info("create yeast return");
        model.addAttribute("pagetitle", "Create Yeast");

        return "yeast/createBY";
    }
    
    //batch create yeast post
    @PostMapping("/yeast/createBY")
    public String Create_Batch_Yeast(@RequestParam("fileInput") MultipartFile file, Model model, @AuthenticationPrincipal Users user, RedirectAttributes redirectAttributes){
        boolean hasErrors = false;
        
        log.info("batch create yeast");
         
         try{
            yeastService.createBatchYeast(file, user);
         }catch(Exception e){
            hasErrors = true;
            
            log.info(String.format("Batch create Yeast exception: %s", e.getMessage()));
            redirectAttributes.addFlashAttribute("error", e.getMessage());
         }
        
         if(hasErrors){
            return "redirect:/yeast/createBY";
         }else{
            return "redirect:/yeast";
         }
        
    }

    
    @GetMapping("/yeast/{id}")
    public String Show_yeast(@PathVariable Integer id, Model model) {
        log.info("show yeast return");
        model.addAttribute("pagetitle", "Show Yeast");

        Yeast yeast = yeastService.getYeastById(id);


        //no record found
        if(yeast == null){
            log.info(String.format("No yeast record (id: %s) found, return to yeast listing page", id));
            
            return "redirect:/yeast";
        }



        model.addAttribute("yeast", yeast);
        return "yeast/1";
    }


    // edit yeast record
    @GetMapping("/yeast/updateY/{id}")
    public String Update_yeast(@PathVariable Integer id, Model model, @AuthenticationPrincipal Users user) {
        log.info("update yeast return");
        model.addAttribute("pagetitle", "Update Yeast");

        Yeast yeast = yeastService.getYeastByIdWithUnescapesHTML(id);
        
        //no record found
        if(yeast == null){
            log.info(String.format("No yeast record (id: %s) found, return to yeast listing page", id));
            
            return "redirect:/yeast";
        }

        //user only allow edit his/her own record
        if(user.getId().intValue() != yeast.getUser().getId().intValue()){
            log.info(String.format("This yeast record (id: %s) (uid: %s) does not belong to this login user (uid: %s), cannot edit and return to primers listing page", id, yeast.getUser().getId(), user.getId()));

            return "redirect:/yeast";
        }

        model.addAttribute("yeast", yeast);

        List<Mating_types> mating_types = matingtypesService.getAllMatingTypes();
        model.addAttribute("mating_types", mating_types);

        //convert <br> back to new line (\r\n or \n)
        yeast.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(yeast.getComments()));

        
        // List<Yeast_Markers> yeast_markers = yeastmarkersService.getAllYeastMarkers();
        // model.addAttribute("Yeast_Markers", yeast_markers);

        return "yeast/updateY";
    }

    @GetMapping("/yeast/parent/{id}")
    public String error_parnet(Model model, @PathVariable Integer id) {
        log.info("error parents return");
        model.addAttribute("pagetitle", "parent error");
        return "yeast/parent";
    }

    @ExceptionHandler(InvalidYeastException.class)
    public String handleInvalidYeastException(InvalidYeastException e, Model model) {
        // Handle the exception, e.g., set an error message in the model
        model.addAttribute("errorMessage", e.getMessage());

        return "yeast/parent";
    }


    @PostMapping(value = "/yeast/updateY/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Update_yeast(@PathVariable("id") Integer id, @ModelAttribute("yeast") @Valid Yeast yeast,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        log.info("update yeast");

        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            // when it pass the vaildation, then try to do update.
            try {
                yeastService.updateYeast(id, yeast);
            }catch (InvalidYeastException e) {
                log.info(String.format("Update yeast exception: %s", e.getMessage()));
                hasErrors = true;
                //throw e;
                bindingResult.reject("UpdateError", e.getMessage());
            }           
            catch (Exception e) {
                log.info(String.format("Update yeast exception: %s", e.getMessage()));
                hasErrors = true;
                bindingResult.reject("UpdateError", e.getMessage());
            }
        }

        // either vaildation error or update error
        if (hasErrors) {
            // validate fail
            List<Mating_types> mating_types = matingtypesService.getAllMatingTypes();
            model.addAttribute("mating_types", mating_types);

            //convert <br> back to new line (\r\n or \n)
            yeast.setComments(ApplicationUtils.HtmlBrToSystemLineSeparator(yeast.getComments()));

            
            // List<Yeast_Markers> yeast_markers = yeastmarkersService.getAllYeastMarkers();
            // model.addAttribute("Yeast_Markers", yeast_markers);

            return "yeast/updateY";
        } else {
            // boolean hasNonMatchingParent = true;
            // List<String> parent_ids_database = yeastrepository.search_parentId();
            // // System.out.print(parent_ids_database);
            // // System.out.print(parent_input);
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
            //     System.out.print("hiiiiiiiiiiiiiiiiiiiiiiiiii");
            //     hasNonMatchingParent = false;
            //     redirectAttributes.addFlashAttribute("ok_message", "Update success.");

            // return "redirect:/yeast/" + id;
            // }
            // else{
            //     log.info(String.format("Update yeast exception: can't find parent name"));
            //     return "yeast/updateY";
            // }
                redirectAttributes.addFlashAttribute("ok_message", "Update success.");
                return "redirect:/yeast/" + id;
            }

        }
}
