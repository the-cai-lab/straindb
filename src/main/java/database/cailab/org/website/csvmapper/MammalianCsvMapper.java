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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MammalianCsvMapper {
    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "cell_line*")
    private String cell_line;

    @CsvBindByName(column = "passage_number*")
    private Integer passage_number;

    @CsvBindByName(column = "cell_type")
    private String cell_type;
    
    @CsvBindByName(column = "karyotype")
    private String karyotype;    

    @CsvBindByName(column = "genotype")
    private String genotype;    

    @CsvBindByName(column = "source")
    private String source;    

    @CsvBindByName(column = "marker")
    private String marker;    

    @CsvBindByName(column = "media*")
    private String media;   
    
    @CsvBindByName(column = "species*")
    private Integer species;   

    @CsvBindByName(column = "location")
    private String location;

    @CsvBindByName(column ="frozen_date*")
    private String datefrozen;

    @CsvBindByName(column = "comments")
    private String comments;    
}
