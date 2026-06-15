#!/bin/bash
# Copyright 2024-2026 The Cai Lab
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


CURR_DATE=$(date +%Y%m%d_%H%M)

export db_username="straindb"
export db_password="itphIpdeph4on"
export main_path="/straindb"
export app_path="${main_path}/app"
export file_upload="${main_path}/"
export file_upload_tmp="${main_path}/tmp"
export ssl_key_store_path="/etc/ssl/certs/keystore.p12"
export ssl_key_store_password="pi&Floibs4"
export application_domain="https://192.168.1.10"

cd ${app_path}

##  Uncomment out one of the following
##  Development version
#VERSION="mysqldev"
##  Production version
VERSION="prod"

mvn clean package -D skipTests -P ${VERSION} >${main_path}/${CURR_DATE}-compile.log 2>&1

##  Show the last 50 lines of the log file
tail -n 50 ${main_path}/${CURR_DATE}-compile.log

printf "=================================================================\n"
printf "Compilation complete.  See %s/%s-compile.log for the entire log file.\n" ${main_path} ${CURR_DATE}
printf "=================================================================\n"
