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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import database.cailab.org.website.entity.Users;
import lombok.extern.log4j.Log4j2;

import org.springframework.ui.Model;

@Log4j2
@Controller
public class HomeIndexController {

    @GetMapping("/")
    public String homeIndex(Model model, @AuthenticationPrincipal Users user) {
        if(user == null){
            //user not yet login
            log.info("home/index return");
            model.addAttribute("pagetitle", "Home");
            return "home/index";
        }else{
            //user already login
            log.info("user already login, home/indexSignin return");
            model.addAttribute("pagetitle", "Home");
            return "home/indexSignin";
        }
        
    }

    // get mapping for the signed in main page
    @GetMapping("/indexSignin")
    public String homeIndexSignin(Model model) {
        log.info("home/indexSignin return");
        model.addAttribute("pagetitle", "Home");
        return "home/indexSignin";
    }
}