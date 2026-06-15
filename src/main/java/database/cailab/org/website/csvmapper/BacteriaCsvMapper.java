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
public class BacteriaCsvMapper {
    @CsvBindByName(column = "plasmid_name")
    private String plasmid_name;
    
    @CsvBindByName(column = "alternate_name")
    private String alternate_name;

    @CsvBindByName(column = "genotype")
    private String genotype;
    
    @CsvBindByName(column = "host_strain")
    private String host_strain;    
    
    @CsvBindByName(column = "bacterial_marker_id")
    private Integer bacterial_marker_id;
    
    @CsvBindByName(column = "other_bacterial_marker")
    private String other_bacterial_marker;
    
    @CsvBindByName(column = "location")
    private String location;    
    
    @CsvBindByName(column = "comments")
    private String comments;
    
}
