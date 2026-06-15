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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import database.cailab.org.website.entity.UserRole;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.service.BacteriaService;
import database.cailab.org.website.service.MammalianService;
import database.cailab.org.website.service.PrimersService;
import database.cailab.org.website.service.UserRoleService;
import database.cailab.org.website.service.UsersService;
import database.cailab.org.website.service.YeastService;
import database.cailab.org.website.service.PlantsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Log4j2
@Controller
public class UsersController {

    private final UsersService usersService;
    private final BacteriaService bacteriaService;
    private final PrimersService primersService;
    private final YeastService yeastService;
    private final MammalianService mammalianService;
    private final PlantsService plantsService;
    private final UserRoleService userRoleService;

    @Autowired
    public UsersController(UsersService usersService, BacteriaService bacteriaService,
            PrimersService primersService, YeastService yeastService, MammalianService mammalianService, PlantsService plantsService,
            UserRoleService userRoleService) {
        this.usersService = usersService;
        this.bacteriaService = bacteriaService;
        this.primersService = primersService;
        this.yeastService = yeastService;
        this.mammalianService = mammalianService;
        this.plantsService = plantsService;
        this.userRoleService = userRoleService;
    }

    // get mapping for the signed in main page
    @GetMapping("/users")
    public String users(Model model) {
        log.info("users/index return");
        model.addAttribute("pagetitle", "Users");

        model.addAttribute("users", usersService.getAllUsers());

        model.addAttribute("userBacteriaCountDto", bacteriaService.getUserWithBacteriaCount());
        model.addAttribute("userPrimersCountDto", primersService.getUserWithPrimersCount());
        model.addAttribute("userYeastCountDto", yeastService.getUserWithYeastCount());
        model.addAttribute("userMammalianCountDto", mammalianService.getUserWithMammalianCount());
        model.addAttribute("userPlantsCountDto", plantsService.getUserWithPlantsCount());

        return "users/index";
    }

    // request reset password
    @PostMapping(value = "/users/requestresetpassword", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String RequestRestPassword(@RequestParam("user_id") String email, Model model,
            RedirectAttributes redirectAttributes, @AuthenticationPrincipal Users user,
            @Value("${application.domain}") String domain) {
        log.info("generate reset password URL");
        try {
            String resetPasswordURL = usersService.requestResetPassword(email, user, domain);
            redirectAttributes.addFlashAttribute("resetpasswordURL", resetPasswordURL);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("resetpassword_error", e.getMessage());
        }

        return "redirect:/users";

    }

    // reset password page
    @GetMapping("/reset-password")
    public String ResetPassword(@RequestParam(value = "t", defaultValue = "") String token, Model model) {
        log.info("resetpassword/resetpassword return");
        boolean isTokenValid = usersService.validToken(token, 0);
        if (!isTokenValid) {
            model.addAttribute("error", "Token expired");
        } else {
            model.addAttribute("resetpasswordtoken", token);
        }
        return "resetpassword/resetpassword";
    }

    // reset password page submit
    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String SubmitNewPassword(@RequestParam("resetpasswordtoken") String token,
            @RequestParam("newpassword") String password, Model model,
            RedirectAttributes redirectAttributes) {
        log.info("update new password");
        try {
            usersService.updateNewPassword(token, password);
            redirectAttributes.addFlashAttribute("updatenewpassword_ok", "Password updated successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reset-password?t=" + token;
    }

    //show single user
    @GetMapping("/users/{id}")
    public String Show_Users(@PathVariable Integer id, Model model) {
        log.info("show users return");
        model.addAttribute("pagetitle", "Show Users");

        Users users = usersService.getUsersById(id);

        // no record found
        if (users == null) {
            log.info(String.format("No users record (id: %s) found, return to users listing page", id));
            return "redirect:/users";
        }

        model.addAttribute("users", users);
        return "users/1";
    }

    // edit users record
    @GetMapping("/users/updateU/{id}")
    public String Update_users(@PathVariable Integer id, Model model) {
        log.info("update users return");
        model.addAttribute("pagetitle", "Update users");

        Users users = usersService.getUsersById(id);

        // no record found
        if (users == null) {
            log.info(String.format("No users record (id: %s) found, return to users listing page", id));

            return "redirect:/users";
        }

        model.addAttribute("users", users);

        List<UserRole> userRole = userRoleService.getAllUserRole();
        model.addAttribute("userrole", userRole);
        return "users/updateU";
    }

    @PostMapping(value = "/users/updateU/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Update_users(@PathVariable("id") Integer id, @ModelAttribute("users") @Valid Users users,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, @RequestParam("password") String password) {

        log.info("update users");
        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            // when it pass the vaildation, then try to do update.
            try {
                usersService.updateUsers(id, users, password);
            } catch (Exception e) {
                log.info(String.format("Update users exception: %s", e.getMessage()));
                
                hasErrors = true;
                bindingResult.reject("CreateError", e.getMessage());
            }
        }
        // either vaildation error or update error
        if (hasErrors) {
            List<UserRole> userRole = userRoleService.getAllUserRole();
            model.addAttribute("userrole", userRole);
            return "users/updateU";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "Update success.");

            return "redirect:/users/" + id;
        }

    }

    // create new users
    @GetMapping("/users/createU")
    public String Create_users(Model model) {
        log.info("create users return");
        model.addAttribute("pagetitle", "Create users");

        Users users = new Users();
        model.addAttribute("users", users);

        List<UserRole> userRole = userRoleService.getAllUserRole();
        model.addAttribute("userrole", userRole);

        return "users/createU";
    }

    // handle create new user form data
    @PostMapping(value = "/users/createU", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String Create_users(@ModelAttribute("users") @Valid Users users,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.info("create users");

        boolean hasErrors = bindingResult.hasErrors();

        if (!hasErrors) {
            // save new user
            try {
                usersService.createUsers(users, request);
            } catch (Exception e) {
                hasErrors = true;
                log.info(String.format("Create users exception: %s", e.getMessage()));
                bindingResult.reject("CreateError", e.getMessage());
            }
        }

        if (hasErrors) {
            List<UserRole> userRole = userRoleService.getAllUserRole();
            model.addAttribute("userrole", userRole);
            
            //clear password before back to the form.
            users.setEncrypted_password("");

            return "users/createU";
        } else {
            redirectAttributes.addFlashAttribute("ok_message", "create success.");

            return "redirect:/users";
        }

    }
}