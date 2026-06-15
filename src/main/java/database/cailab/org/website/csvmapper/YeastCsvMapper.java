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

package database.cailab.org.website.csvmapper;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YeastCsvMapper {
    @CsvBindByName(column = "genotype*")
    private String genotype;
    
    @CsvBindByName(column = "other_names")
    private String other_names;
    
    @CsvBindByName(column = "plasmid")
    private String plasmid;    
    
    @CsvBindByName(column = "plasmid_type")
    private String plasmid_type;
    
    @CsvBindByName(column = "mating_type_id")
    private Integer mating_type_id;
    
    @CsvBindByName(column = "other_mating_type")
    private String other_mating_type;  
    
    @CsvBindByName(column = "parent_name1")
    private String parent_name1;
    
    @CsvBindByName(column = "parent_name2")
    private String parent_name2;

    @CsvBindByName(column = "markers_list")
    private String markers_list;

    @CsvBindByName(column = "location")
    private String location;

    @CsvBindByName(column = "comments")
    private String comments;
    
}
