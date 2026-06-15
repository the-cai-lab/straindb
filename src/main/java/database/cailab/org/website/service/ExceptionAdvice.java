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

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/*
 *  This class use to handle exceptoin globally
 */

 @ControllerAdvice
public class ExceptionAdvice{
    /*
     * Handle Max Upload Size Exceeded Exception
     * Use request header: referer to check where it come from 
     * Then it will redirect back to that page with an error message
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        //System.out.println("ref: " +request.getHeader("referer"));
        redirectAttributes.addFlashAttribute("error", "File too large.");
        return "redirect:" + request.getHeader("referer");
    }
}
