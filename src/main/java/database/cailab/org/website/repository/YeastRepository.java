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

import database.cailab.org.website.dto.UserYeastCountDto;
import database.cailab.org.website.entity.Yeast;

public interface YeastRepository extends JpaRepository<Yeast, Integer> {
    // Repository methods
    Optional<Yeast> findById(Integer id);

    //return yeast order by id asc
    List<Yeast> findAllByOrderByIdAsc();

    //return yeast order by id desc
    List<Yeast> findAllByOrderByIdDesc();
    
    //return yeast order by id asc
    List<Yeast> findAllByOrderByUser_IdAsc();
    
    // return list of yeast under the user or no yeast found
    List<Yeast> findByUser_Id(Integer userID);

    @Query("SELECT u.name AS name,  u.id AS userid, COUNT(y.id) AS numberOfYeast FROM Yeast y JOIN y.user u GROUP BY u.name, u.id ORDER BY u.name")
    List<UserYeastCountDto> findYeastCountByUserOrderByName();

    @Query("SELECT y.personal_id  FROM Yeast y")
    List<String> search_parentId();

    
    Optional<Yeast> findTopByOrderByIdDesc();

    int countByUser_id(Integer user_id);

    @Query("select count(y) from Yeast y WHERE y.lab_id LIKE %:labID%")
    int countByLabID(Integer labID);

    @Query("select count(y) from Yeast y JOIN y.user u WHERE y.personal_id LIKE :personalID% and u.id = :userID")
    int countByPersonalID(Integer personalID, Integer userID);

    //find the latest Yeast record of the user and return the personal id of that record
    @Query("SELECT y.personal_id as personal_id FROM Yeast y JOIN y.user u WHERE u.id = :userID ORDER BY y.id DESC LIMIT 1")
    String latestRecordPersonalID(Integer userID);
}
