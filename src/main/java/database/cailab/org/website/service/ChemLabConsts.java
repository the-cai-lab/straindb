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

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

public final class ChemLabConsts {
    public static final String LAB_INITIALS = "YC";
    public static final String BACTERIA_PREFIX = "e";
    public static final String PRIMERS_PREFIX = "p";
    public static final String YEAST_PREFIX = "y";
    public static final String MAMMALIAN_PREFIX = "m";
    public static final String PLANTS_PREFIX = "n";
    public static final int TOKEN_VAILD_TIME = 60;

    public static final String PAGE_TITLE = "My Lab Page";
    public static final String PAGE_TEXT_LOGO = "The<br>My<br>Lab";

    public static final String[] ALLOW_FILE_TYPE = new String[]{".csv", ".txt", ".dna", ".gb", ".gbk", ".fasta", ".fa", ".fna", ".fas", ".ffn", ".faa", ".mpfa", ".frn"};
    
    //prevent concurrent create / batch create of record. 
    public static final ReentrantLock bacteriaCreateLock =  new ReentrantLock();
    public static final ReentrantLock primersCreateLock = new ReentrantLock();
    public static final ReentrantLock yeastCreateLock = new ReentrantLock();
    public static final ReentrantLock mammalianCreateLock = new ReentrantLock();
    public static final ReentrantLock plantsCreateLock = new ReentrantLock();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();



    //use to generate reset password token
    public static String  generateToken() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return BASE64_ENCODER.encodeToString(randomBytes);
    }

    private ChemLabConsts(){

    }
}
