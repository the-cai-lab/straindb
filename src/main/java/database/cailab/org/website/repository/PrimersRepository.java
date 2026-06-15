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

import database.cailab.org.website.dto.UserPrimersCountDto;
import database.cailab.org.website.entity.Primers;

public interface PrimersRepository extends JpaRepository<Primers, Integer> {
    // Repository methods
    Optional<Primers> findById(Integer id);

    //return primers order by id asc
    List<Primers> findAllByOrderByIdAsc();

    //return primers order by id desc
    List<Primers> findAllByOrderByIdDesc();
    
    //return primers order by id asc
    List<Primers> findAllByOrderByUser_IdAsc();
    
    // return list of primers under the user or no primers found
    List<Primers> findByUser_Id(Integer userID);

    @Query("SELECT u.name AS name,  u.id AS userid, COUNT(p.id) AS numberOfPrimers FROM Primers p JOIN p.user u GROUP BY u.name, u.id ORDER BY u.name")
    List<UserPrimersCountDto> findPrimersCountByUserOrderByName();


    Optional<Primers> findTopByOrderByIdDesc();

    int countByUser_id(Integer user_id);

    @Query("select count(p) from Primers p WHERE p.lab_id LIKE %:labID%")
    int countByLabID(Integer labID);

    @Query("select count(p) from Primers p JOIN p.user u WHERE p.personal_id LIKE :personalID% and u.id = :userID")
    int countByPersonalID(Integer personalID, Integer userID);

    //find the latest Primers record of the user and return the personal id of that record
    @Query("SELECT p.personal_id as personal_id FROM Primers p JOIN p.user u WHERE u.id = :userID ORDER BY p.id DESC LIMIT 1")
    String latestRecordPersonalID(Integer userID);
}
