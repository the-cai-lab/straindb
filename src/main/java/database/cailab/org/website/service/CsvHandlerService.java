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

import java.util.List;

import database.cailab.org.website.csvmapper.BacteriaCsvMapper;
import database.cailab.org.website.csvmapper.PrimersCsvMapper;
import database.cailab.org.website.csvmapper.YeastCsvMapper;
import database.cailab.org.website.csvmapper.MammalianCsvMapper;
import database.cailab.org.website.csvmapper.PlantsCsvMapper;

public interface CsvHandlerService {
    List<BacteriaCsvMapper> readBacteriaCsv(String fileName) throws Exception;

    List<PrimersCsvMapper> readPrimersCsv(String fileName) throws Exception;

    List<YeastCsvMapper> readYeastCsv(String fileName) throws Exception;

    List<MammalianCsvMapper> readMammalianCsv(String fileName) throws Exception;

    List<PlantsCsvMapper> readPlantsCsv(String fileName) throws Exception;

}
