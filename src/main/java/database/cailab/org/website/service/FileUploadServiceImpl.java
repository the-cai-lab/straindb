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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FileUploadServiceImpl implements FileUploadService{
    //@Value("${file.upload.base-path}")
    private final Path rootUploadLocation;
    private final Path rooCsvLocation;
    private final String backupExtension = ".backup";

    public FileUploadServiceImpl(@Value("${file.upload.base-path}") String rootLocation){
        rootUploadLocation = Paths.get(rootLocation + "upload" + File.separator);
        rooCsvLocation = Paths.get(rootLocation + "csv" + File.separator);
    }

    
    @Override
    public String saveFile(MultipartFile file, String subDirectory) throws Exception{
            Optional <String> filename = Optional.of(file.getOriginalFilename());
            String originalFilename = "";

            //Check file name
            if(filename.isPresent()){
              originalFilename = filename.get().toLowerCase();
            }else{
               throw new Exception("File name is missing"); 
            }

            //check allow upload file type
            if(!isAllowFileType(originalFilename)){
                throw new Exception("File type not allow");
            }
            
            // Append the file name with current time in milliseconds to prevent duplicate file names and overwriting each other
            //original file + '-' + currentTimeMillis + '.csv'
            //eg: bacteria-template-1234567.csv
            //**Only apply to csv file (upload from batch create)

            // For other file types, we use its original filename i.e filename.get()
            originalFilename = (originalFilename.endsWith(".csv")) ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) + "-" + System.currentTimeMillis() + ".csv" : filename.get();

            //csv upload to csv folder
            Path rootlocation = (originalFilename.endsWith(".csv")) ? rooCsvLocation : rootUploadLocation;
        
            Path destinationFile = rootlocation.resolve(Path.of(subDirectory + File.separator + originalFilename)).normalize().toAbsolutePath();
            
            // This is a security check
            if (!destinationFile.getParent().equals(rootlocation.resolve(Path.of(subDirectory)).toAbsolutePath())) {
                throw new Exception("Cannot store file outside current directory.");
			}

            //create subdirectory if it is not exist
            try {
                Files.createDirectories(destinationFile.getParent());
            } catch (IOException e) {
               throw e;
            }



            // Handle old files in the directory
            ArrayList<Path> allFiles = null;
            boolean deleteOldFiles = true;
            
            // We only forcus on the upload directory other than csv upload directory
            if(!originalFilename.endsWith(".csv")) {
                allFiles = new ArrayList<>();
                getAllFilesInCurrentDir(destinationFile.getParent(), allFiles);

                if(allFiles.size() > 0)  {
                    // If there are old files, we backup them
                    handleOldFiles(allFiles, FileAction.BACKUP);    
                }
            }
            
            //save the file (convert csv file with UTF-8 BOM to UTF-8 and save it)
            try (InputStream inputStream = file.getInputStream()) {
                BOMInputStream bomIn = BOMInputStream.builder().setInputStream(inputStream).setInclude(false).get();
                Files.copy(bomIn, destinationFile, StandardCopyOption.REPLACE_EXISTING);  
            }catch (IOException e) {
                // recovery the backup files if there is an error when saving the file
                if(allFiles != null && allFiles.size() > 0) {
                    log.info("Error when saving file, we will restore the old files");
                    handleOldFiles(allFiles, FileAction.RESTORE);
                    deleteOldFiles = false; // Do not delete old files if there is an error
                }
            }
            
            // New non-csv file is saved, we delete the old files
            if(allFiles != null && allFiles.size() > 0 && deleteOldFiles) {
                handleOldFiles(allFiles, FileAction.DELETE);
            }

            return originalFilename;
    }

    private boolean isAllowFileType(String fullFileName){
        return Arrays.stream(ChemLabConsts.ALLOW_FILE_TYPE).anyMatch(filetype -> fullFileName.endsWith(filetype));
    }

    private void getAllFilesInCurrentDir(Path directory, ArrayList<Path> allFiles) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {   
            for (Path entry : paths){
                // We are expecting the entry to be a file, not a directory (It shouldn't contain subdirectories, if it does, please check with the administrator)
                allFiles.add(entry);
            }
        } 
    }

    private void handleOldFiles(ArrayList<Path> allFiles, FileAction fileAction) throws IllegalArgumentException {
        switch (fileAction) {
            case BACKUP:
            case RESTORE:
                allFiles.forEach(filePath -> {
                    try {
                        // rename file
                        if (fileAction == FileAction.BACKUP) {
                            Files.move(filePath, filePath.resolveSibling(filePath.getFileName() + backupExtension));
                        }else{
                            // remove the ".backup" extension at the end of the file name
                            Files.move(filePath, filePath.resolveSibling(filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf("."))));   
                        }
                    } catch (IOException e) {
                        log.error("Error handling old file: " + e.toString());
                    }
                });
                break;
            case DELETE:
                // delete all files
                allFiles.forEach(filePath -> {
                    try {
                        log.info(filePath.resolveSibling(filePath.getFileName().toString() + backupExtension) + " will be deleted");
                        boolean canDelete = Files.deleteIfExists(filePath.resolveSibling(filePath.getFileName().toString() + backupExtension));
                        log.info("File deleted: " + canDelete);
                    } catch (IOException e) {
                        log.error("Error deleting file: " +  e.toString());
                    }
                });
                break;
            default:
                throw new IllegalArgumentException("Invalid action");    
        }
    }
}
