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

import database.cailab.org.website.dto.UserPrimersCountDto;
import database.cailab.org.website.entity.Primers;
import database.cailab.org.website.entity.Users;

public interface PrimersService {
    public List<Primers> getAllPrimers();

    public List<Primers> getAllPrimersOrderByIdAsc();

    public List<Primers> getAllPrimersOrderByIdDesc();

    public List<Primers> getAllPrimersOrderByUser_IdAsc();

    public List<Primers> getAllPrimersByUserId(Integer userID);

    public List<UserPrimersCountDto> getUserWithPrimersCount();

    public Primers getPrimersById(Integer id);

    //decode attribute contain html special char before return
    public Primers getPrimersByIdWithUnescapesHTML(Integer id);

    public Primers updatePrimers(Integer id, Primers primers);

    public Primers getLastPrimersRecord();

    public Primers createPrimers(Primers primers, Users user) throws Exception;

    public List<Primers> createBatchPrimers(MultipartFile file, Users user) throws Exception;

}
