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

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import database.cailab.org.website.dto.UserBacteriaCountDto;
import database.cailab.org.website.entity.Bacteria;

public interface BacteriaRepository extends JpaRepository<Bacteria, Integer> {
    // Repository methods
    Optional<Bacteria> findById(Integer id);

    //return bacteria order by id asc
    List<Bacteria> findAllByOrderByIdAsc();

    //return bacteria order by id desc
    List<Bacteria> findAllByOrderByIdDesc();
    
    //return bacteria order by id asc
    List<Bacteria> findAllByOrderByUser_IdAsc();
    
    // return list of bacteria under the user or no bacteria found
    List<Bacteria> findByUser_Id(Integer userID);

    //count total number of bateria for earch person
    //@Query(value = "select users.name as name, count(bacteria.user_id) as numberOfBacteria from bacteria join users on bacteria.user_id = users.id group by users.name order by users.name", nativeQuery = true)
    @Query("SELECT u.name AS name,  u.id AS userid, COUNT(b.id) AS numberOfBacteria FROM Bacteria b JOIN b.user u GROUP BY u.name, u.id ORDER BY u.name")
    List<UserBacteriaCountDto> findBacteriaCountByUserOrderByName();

    //find the last record of bacteria
    Optional<Bacteria> findTopByOrderByIdDesc();

    int countByUser_id(Integer user_id);

    @Query("select count(b) from Bacteria b WHERE b.lab_id LIKE %:labID%")
    int countByLabID(Integer labID);
    
    @Query("select count(b) from Bacteria b JOIN b.user u WHERE b.personal_id LIKE :personalID% and u.id = :userID")
    int countByPersonalID(Integer personalID, Integer userID);

    //find the latest bacteria record of the user and return the personal id of that record
    @Query("SELECT b.personal_id as personal_id FROM Bacteria b JOIN b.user u WHERE u.id = :userID ORDER BY b.id DESC LIMIT 1")
    String latestRecordPersonalID(Integer userID);
}
