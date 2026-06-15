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

package database.cailab.org.website.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;


import database.cailab.org.website.entity.Mammalian;
import database.cailab.org.website.dto.UserMammalianCountDto;

public interface MammalianRepository extends JpaRepository<Mammalian, Integer> {
    @NonNull
    Optional<Mammalian> findById(@NonNull Integer id);

    // Return all Mammalian records ordered by id in descending order
    List<Mammalian> findAllByOrderByIdDesc();

    //count total number of mammalian for earch person
    @Query("SELECT u.name AS name, u.id AS userid, COUNT(mam.id) AS numberOfMammalians FROM Mammalian mam JOIN mam.user u GROUP BY u.name, u.id ORDER BY u.name") 
    List<UserMammalianCountDto> findMammalianCountByUserOrderByName();

    //find the last record of Mammalian
    Optional<Mammalian> findTopByOrderByIdDesc();

    @Query("select count(mam) from Mammalian mam WHERE mam.lab_id LIKE %:labID%")
    int countByLabID(String labID);

    @Query("select count(mam) from Mammalian mam JOIN mam.user u WHERE mam.personal_id LIKE :personalID% and u.id = :userID")
    int countByPersonalID(Integer personalID, Integer userID);

    //find the latest Mammalian record of the user and return the personal id of that record
    @Query("SELECT mam.personal_id as personal_id FROM Mammalian mam JOIN mam.user u WHERE u.id = :userID ORDER BY mam.id DESC LIMIT 1")
    String latestRecordPersonalID(Integer userID);
    
    
}
