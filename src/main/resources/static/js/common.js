/**
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

function TimeFormatHandler(selectedDateTime){
    //yyyy-mm-dd hh:mm:ss
    let patternWithMilliseconds = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
    //yyyy-mm-dd hh:dd
    let patternWithoutMilliseconds = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/;

    //Get the current datetime value and append '.000' for second and millisecond
    let datetimeValue = selectedDateTime;

    if (patternWithMilliseconds.test(datetimeValue)) {
        //add back millisecond
        datetimeValue = datetimeValue + ".000";
    } else if (patternWithoutMilliseconds.test(datetimeValue)) {
        //add back second and millisecond
        datetimeValue = datetimeValue + ":00.000";
    }

    return datetimeValue;
} 
