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

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import database.cailab.org.website.entity.Mammalian;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.dto.UserMammalianCountDto;

public interface MammalianService {

    public List<Mammalian> getAllMammaliansOrderByIdDesc();

    public List<UserMammalianCountDto> getUserWithMammalianCount();

    public Mammalian getMammalianById(Integer id);

    public Mammalian createMammalian(Mammalian mammalian, Users user) throws Exception;

    public Mammalian getMammalianByIdWithUnescapesHTML(Integer id);

    public Mammalian updateMammalian(Integer id, Mammalian mammalian) throws Exception;

    public List<Mammalian> createBatchMammalian(MultipartFile file, Users user) throws Exception;
    
} 
