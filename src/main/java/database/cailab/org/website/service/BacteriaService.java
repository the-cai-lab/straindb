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

import database.cailab.org.website.dto.UserBacteriaCountDto;
import database.cailab.org.website.entity.Bacteria;
import database.cailab.org.website.entity.Users;

public interface BacteriaService {
    public List<Bacteria> getAllBacteria();

    public List<Bacteria> getAllBacteriaOrderByIdAsc();

    public List<Bacteria> getAllBacteriaOrderByIdDesc();

    public List<Bacteria> getAllBacteriaOrderByUser_IdAsc();

    public List<Bacteria> getAllBacteriaByUserId(Integer userID);

    public List<UserBacteriaCountDto> getUserWithBacteriaCount();

    public Bacteria getBacteriaById(Integer id);

    //decode attribute contain html special char before return
    public Bacteria getBacteriaByIdWithUnescapesHTML(Integer id);

    public Bacteria updateBacteria(Integer id, Bacteria bacteria, MultipartFile file) throws Exception;

    public Bacteria getLastBacteriaRecord();

    public Bacteria createBacteria(Bacteria bacteria, Users user) throws Exception;

    public List<Bacteria> createBatchBacteria(MultipartFile file, Users user) throws Exception;

}
