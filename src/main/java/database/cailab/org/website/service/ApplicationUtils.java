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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Timestamp;
import java.time.Instant;

public class ApplicationUtils {
    public static String HtmlBrToSystemLineSeparator(String text){
        //get the System line.separator: \r\n or \n
        String lineSeparator = System.getProperty("line.separator");
        return text.replaceAll("<br>", lineSeparator);
    }

    public static int ExtraDigitAndPlusOne(String text) throws Exception{
        /*
         * Use regex to extract the digit part (only extract last part of digit)
         * Bewarem if there is duplicate initials , there will be extra number after the initial eg TZ142e010
         * Then use the extract value + 1 to form the next id
         */

        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String lastIDNumber = matcher.group();
            
            return Integer.parseInt(lastIDNumber) + 1;
        } else {
            throw new Exception("Input ID format not correct");
        }
    }


    public static String ConvertNewLineToBr(String text) {
        // Convert new line characters to <br> tags
        return text.replaceAll("\r\n|\n", "<br>");
    }

    public static Timestamp getCurrentTimestamp() {
        return Timestamp.from(Instant.now());
    }

    public static Timestamp ConvertStringToTimestamp(String dateString) throws Exception {
        // dateString can either be in the format "dd-MM-yyyy" or "yyyy-MM-dd"
        // Convert the date string to a Timestamp object
        
        if (dateString == null || dateString.isEmpty()) {
            throw new Exception("Date string cannot be null or empty");
        }
        
        if (!dateString.matches("\\d{2}-\\d{2}-\\d{4}") && !dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new Exception("Date string must be in the format 'yyyy-MM-dd' or 'dd-MM-yyyy'");
        }

        // If the format is 'yyyy-MM-dd', we can directly convert it
        if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return Timestamp.valueOf(dateString + " 00:00:00");
        } else {
            // If the format is 'dd-MM-yyyy', we need to rearrange it to 'yyyy-MM-dd'
            String[] parts = dateString.split("-");
            String reformatted = parts[2] + "-" + parts[1] + "-" + parts[0];
            return Timestamp.valueOf(reformatted + " 00:00:00");
        }
    }
}
