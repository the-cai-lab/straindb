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
import org.springframework.lang.NonNull;

import database.cailab.org.website.entity.Plants;
import database.cailab.org.website.dto.UserPlantsCountDto;

public interface PlantsRepository extends JpaRepository<Plants, Integer> {
    // This interface will automatically provide CRUD operations for Plants entities
    // Additional custom query methods can be defined here if needed
    @NonNull
    Optional<Plants> findById(@NonNull Integer id);

    // Return all Plants records ordered by id in descending order
    List<Plants> findAllByOrderByIdDesc();

    // Count total number of plants for each person
    @Query("SELECT u.name AS name, u.id AS userid, COUNT(pl.id) AS numberOfPlants FROM Plants pl JOIN pl.user u GROUP BY u.name, u.id ORDER BY u.name")
    List<UserPlantsCountDto> findPlantsCountByUserOrderByName();

    //find the last record of Plants
    Optional<Plants> findTopByOrderByIdDesc();

    @Query("select count(pl) from Plants pl WHERE pl.lab_id LIKE %:labID%")
    int countByLabID(String labID);

    @Query("select count(pl) from Plants pl JOIN pl.user u WHERE pl.personal_id LIKE :personalID% and u.id = :userID")
    int countByPersonalID(Integer personalID, Integer userID);

    @Query("SELECT pl.personal_id as personal_id FROM Plants pl JOIN pl.user u WHERE u.id = :userID ORDER BY pl.id DESC LIMIT 1")
    String latestRecordPersonalID(Integer userID);
    
} 