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

// $(document).ready(function () {
//     $(".bacteria__btn--resetPassword").on("click", function(){
//         //get the email
//         let value = $(this).closest("tr").find("th:eq(1) p").text();
//         //get the csrf token
//         let csrf = $('input[name="_csrf"]').val();
//         //console.log($(this).closest("tr").find("th:eq(1) p").text());
        
//         $('<form action="/users/requestresetpassword" method="POST">' + 
//           '<input type="hidden" name="user_id" value="' + value + '">' +
//           '<input type="hidden" name="_csrf" value="' + csrf + '">' +
//           '</form>').appendTo('body').submit();
        
//     });
// });




$(document).ready(function () {
    // default column of ordering
    let default_order = 10; // user column, index starts at 0

    // DataTable initialization
    let user_table = $('#users_table').DataTable({
        paging: false,
        // keep it false so the reset button works
        dom: 'lrtip',
        ordering: true,
        // keep it false for now because there is a glitch at the end column
        columns: [
            { data: 'name'},
            { data: 'email'},
            { data: 'role_id'},
            { data: 'deactivate'},
            { data: 'bacteria'},
            { data: 'primers'},
            { data: 'yeast'},
            { data: 'mammalian'},
            { data: 'plants'},
            { data: 'Id', orderable: true},
            { data: 'user', orderable: true}
        ],
        search: {
            regex: true,
            smart: true,
        },
        order: [
            [default_order, 'desc']
        ],
    });

    // Keyup event handling for search
    $('#userSearch').keyup(function () {
        user_table.search($(this).val()).draw();
    });

    // Reset Password button click event handling
    $(".user__btn--resetPassword").on("click", function () {
        //get the email
        let value = $(this).closest("tr").find("th:eq(1) p").text();
        //get the csrf token
        let csrf = $('input[name="_csrf"]').val();
        //console.log($(this).closest("tr").find("th:eq(1) p").text());

        $('<form action="/users/requestresetpassword" method="POST">' +
            '<input type="hidden" name="user_id" value="' + value + '">' +
            '<input type="hidden" name="_csrf" value="' + csrf + '">' +
            '</form>').appendTo('body').submit();
    });
});
