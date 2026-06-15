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

$(document).ready(function () {
    var datatimepickerOptions = {
        // Other configuration options
        showButtonPanel: true,
        format: 'Y-m-d H:i:s',
        step: 1
    };
    
    
    // Set default value to current date and time for create form
    if($('#primers_create_form').length) {
        datatimepickerOptions.value = new Date();
    }


    // Attach a date and time picker to the input field
    $('#date').datetimepicker(datatimepickerOptions);

    
    $('#primers_edit_form').submit(function () {
        var datetimeValue = $('#date').val();        
        
        //modify datepicker format to require timestamp format    
        datetimeValue = TimeFormatHandler(datetimeValue);
        
        // Update the value of the datetime field before submitting the form
        $('#date').val(datetimeValue);
    });

    
    $('#primers_create_form').submit(function () {
        var datetimeValue = $('#date').val();        
        
        //modify datepicker format to require timestamp format    
        datetimeValue = TimeFormatHandler(datetimeValue);
        
        // Update the value of the datetime field before submitting the form
        $('#date').val(datetimeValue);
    });
});