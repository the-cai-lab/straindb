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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import database.cailab.org.website.csvmapper.BacteriaCsvMapper;
import database.cailab.org.website.csvmapper.PrimersCsvMapper;
import database.cailab.org.website.csvmapper.YeastCsvMapper;
import database.cailab.org.website.csvmapper.MammalianCsvMapper;
import database.cailab.org.website.csvmapper.PlantsCsvMapper;

import java.io.FileInputStream;
import java.io.InputStreamReader;

@Service
public class CsvHandlerServicelmpl implements CsvHandlerService{

    private final Path rooCsvLocation;

    public CsvHandlerServicelmpl(@Value("${file.upload.base-path}") String rootLocation){
        rooCsvLocation = Paths.get(rootLocation + "csv" + File.separator);
    }

    @Override
    public List<BacteriaCsvMapper> readBacteriaCsv(String fileName) throws Exception{
        return readCsv(fileName, BacteriaCsvMapper.class);
    }

    @Override
    public List<PrimersCsvMapper> readPrimersCsv(String fileName) throws Exception{
        return readCsv(fileName, PrimersCsvMapper.class);
    }

    @Override
    public List<YeastCsvMapper> readYeastCsv(String fileName) throws Exception{
        return readCsv(fileName, YeastCsvMapper.class);
    }

    @Override
    public List<MammalianCsvMapper> readMammalianCsv(String fileName) throws Exception{
        return readCsv(fileName, MammalianCsvMapper.class);
    }

    @Override
    public List<PlantsCsvMapper> readPlantsCsv(String fileName) throws Exception{
        return readCsv(fileName, PlantsCsvMapper.class);
    }

    
    //Read different CSV base on input class: BacteriaCsvMapper
    
    /*
     * Please note:
     * Before you call this, please ensure the filename and the file location are both valid 
     * Current flow is call "saveFile" before calling this method.  
     * "saveFile" contain safety check, therefore it should be safe to call this method afterword.
     */
    
    private <T> List<T> readCsv(String fileName, Class<T> obj) throws Exception{
        Path destinationFile = rooCsvLocation.resolve(Path.of("csv" + File.separator + fileName)).normalize().toAbsolutePath();
        System.out.println(destinationFile.toString());
        //Read csv file with UTF-8 encoding
        try (Reader reader = new InputStreamReader(new FileInputStream(destinationFile.toString()), StandardCharsets.UTF_8)) {
            
            //if we use header name mapper, we can skip line 1 (the header line), otherwise, all data cannot be read
            //CsvToBean<T> csvMapper = new CsvToBeanBuilder<T>(reader).withType(obj).withSkipLines(1).withIgnoreLeadingWhiteSpace(true).build();
            
            CsvToBean<T> csvMapper = new CsvToBeanBuilder<T>(reader).withType(obj).withIgnoreLeadingWhiteSpace(true).build();
            return csvMapper.parse();
        }
    }
    
}
