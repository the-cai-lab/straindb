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

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import database.cailab.org.website.service.ChemLabConsts;

@ControllerAdvice
public class GlobalModelAttributes {
    // Default data into your model on every request
    @ModelAttribute
    public void getPageInfo(Model model) {
         // Add text logo to the model
         model.addAttribute("pagetextlogo", ChemLabConsts.PAGE_TEXT_LOGO);
    }
}
