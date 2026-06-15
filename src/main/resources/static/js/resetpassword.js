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

$(document).ready(function() {
    $("#reset_password_form").on("submit", function(event){
        let newpassword = $("#newpassword").val();
        let renewpassword = $("#renewpassword").val();
        
        event.preventDefault();

        if(newpassword!=renewpassword){
            $('#newpasswordError').text('The password and re-type password are not match.');
            $('#newpasswordError').show();
        }else {
            $('#newpasswordError').hide();
            this.submit();
        }
    });
});