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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PerlValidationHandlerServiceImpl implements PerlValidationHandlerService {
    private final String rooCsvLocation;

    public PerlValidationHandlerServiceImpl(@Value("${file.upload.base-path}") String rootLocation){
        //this.rooCsvLocation = rootLocation + "csv" + File.separator;
        this.rooCsvLocation = rootLocation + "csv" + File.separator + "csv";
    }

    @Override
    public StringBuffer dataValidation(String csvDataType, String fileName) throws Exception{
        StringBuffer outputReport = null;
        String reportName = fileName.substring(0, fileName.lastIndexOf('.')) + "-report.txt";
        
        ProcessBuilder processBuilder = new ProcessBuilder("perl", "validate.pl", "--datatype", csvDataType  , "--report", reportName , "--input", fileName , "--output", fileName);
        
        // Set the working directory
        processBuilder.directory(new File(rooCsvLocation));
        
        try{
            Process process = processBuilder.start();
            
            int exitCode = process.waitFor();

            // Print the exit code
            //System.out.println("Exit Code: " + exitCode);

            //temp testing
            if(exitCode != 0){
                //csv file contain error
                //Read the output report
                outputReport = new StringBuffer();
                Path reportPath = Paths.get(rooCsvLocation + File.separator + reportName);
                String report = new String(Files.readAllBytes(reportPath));
                outputReport.append(report.replaceAll("\r\n|\n", "<br>"));
            }

        }catch(Exception e){
            throw e;
        }

        return outputReport;
    }
    
}
