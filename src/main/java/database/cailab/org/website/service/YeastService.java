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

import database.cailab.org.website.dto.UserYeastCountDto;
import database.cailab.org.website.entity.Users;
import database.cailab.org.website.entity.Yeast;

public interface YeastService {
    public List<Yeast> getAllYeast();

    public List<Yeast> getAllYeastOrderByIdAsc();

    public List<Yeast> getAllYeastOrderByIdDesc();

    public List<Yeast> getAllYeastOrderByUser_IdAsc();

    public List<Yeast> getAllYeastByUserId(Integer userID);

    public List<UserYeastCountDto> getUserWithYeastCount();

    public Yeast getYeastById(Integer id);

    //decode attribute contain html special char before return
    public Yeast getYeastByIdWithUnescapesHTML(Integer id);

    public Yeast updateYeast(Integer id, Yeast yeast) throws Exception;

    public Yeast createYeast(Yeast primers, Users user) throws Exception;

    public List<Yeast> createBatchYeast(MultipartFile file, Users user) throws Exception;

}
