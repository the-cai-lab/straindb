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

package database.cailab.org.website.entity;

import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "primers")
public class Primers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

//    private Integer user_id;
    
    @Column(nullable = false)
    @NotEmpty(message = "Description cannot be blank")
    private String description;
    
    @Column(nullable = false)
    @NotEmpty(message = "Sequence cannot be blank")
    private String sequence;

    private Integer melting_temperature;

    @Column(length = 255)
    @Size(max = 255, message = "Concentration is too long, please try again.")
    private String concentration;

    @Column(length = 255)
    @Size(max = 255, message = "Vendor is too long, please try again.")
    private String vendor;

    @Column(length = 255)
    @Size(max = 255, message = "Location is too long, please try again.")
    private String location;

    private Timestamp date;

    @Column(length = 255, nullable = true)
    @Size(max = 255, message = "Comments is too long, please try again.")
    private String comments;

    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;

    @Column(length = 255)
    @Size(max = 255, message = "Lab ID is too long, please try again.")
    private String lab_id;

    @Column(length = 255)
    @Size(max = 255, message = "Personal ID is too long, please try again.")
    private String personal_id;

    // private Integer orientation_Id;

    private Boolean edited;

    private Integer plate_Id;

    @Column(length = 255)
    @Size(max = 255, message = "well Id is too long, please try again.")
    private String well_id;

    private Boolean soft_delete;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "orientation_id")
    private Orientations orientations;
    
    // set default value and data transformation before new primers insert into table
    @PrePersist
    public void onCreate(){
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
       
        created_at = currentTimestamp;
        updated_at = currentTimestamp;
        edited = false;
        soft_delete = false;

        //filter newline characters
        description = description.replaceAll("\r\n|\n", "<br>");
        sequence = sequence.replaceAll("\r\n|\n", "<br>");
        comments = comments.replaceAll("\r\n|\n", "<br>");
    }

    //set default value and data transformation before update primers
    @PreUpdate
    public void onPreUpdate(){
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

        updated_at = currentTimestamp;
        edited = true;

        //filter newline characters
        description = description.replaceAll("\r\n|\n", "<br>");
        sequence = sequence.replaceAll("\r\n|\n", "<br>");
        comments = comments.replaceAll("\r\n|\n", "<br>");
    }

}
