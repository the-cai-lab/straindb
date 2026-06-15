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
@Table(name = "yeasts")
public class Yeast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

//    private Integer user_id;

    @Column(length = 255)
    @Size(max = 255, message = "Other names is too long, please try again.")
    private String other_names;

    @Column(length = 255)
    @Size(max = 255, message = "Plasmid is too long, please try again.")
    private String plasmid;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Genotype is too long, please try again.")
    @NotEmpty(message = "Genotype cannot be blank")
    private String genotype;

    @Column(length = 255)
    @Size(max = 255, message = "Pladmid type is too long, please try again.")
    private String plasmid_type;


    // @Column(length = 255)
    // @Size(max = 255, message = "marker is too long, please try again.")
    // private String marker;

    @Column(length = 255)
    @Size(max = 255, message = "Location is too long, please try again.")
    private String location;

    private String comments;

    private Timestamp date;

    // @Column(nullable = true)
    // private Integer parent_id;

    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;

    @Column(length = 255)
    @Size(max = 255, message = "Other mating type is too long, please try again.")
    private String other_mating_type;

    // @Column(length = 255)
    // @Size(max = 255, message = "Other yeast marker ID is too long, please try again.")
    // private String other_yeast_marker_id;

    @Column(length = 255)
    @Size(max = 255, message = "Lab ID is too long, please try again.")
    private String lab_id;

    @Column(length = 255)
    @Size(max = 255, message = "Personal ID is too long, please try again.")
    private String personal_id;

    private Boolean edited;

    // @Column(length = 255)
    // @Size(max = 255, message = "Parent name is too long, please try again.")
    // private String parent_name;

    @Column(length = 255)
    @Size(max = 255, message = "Parent name is too long, please try again.")
    private String parent_name1;

    @Column(length = 255)
    @Size(max = 255, message = "Parent name is too long, please try again.")
    private String parent_name2;

    @Column(length = 255)
    @Size(max = 255, message = "Markers List is too long, please try again.")
    private String markers_list;

    //wait for table update
    //private Boolean soft_delete;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "mating_type_id")
    private Mating_types mating_types;

    // when the sql is implemented
    // @ManyToOne(fetch =  FetchType.LAZY)
    // @JoinColumn(name = "yeast_makrers")
    // private Yeast_Markers yeast_markers;

    // set default value and data transformation before new yeast insert into table
    @PrePersist
    public void onCreate(){
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

        created_at = currentTimestamp;
        updated_at = currentTimestamp;
        edited = false;
        //soft_delete = false;

        //filter newline characters
        comments = comments.replaceAll("\r\n|\n", "<br>");
    }

    //set default value and data transformation before update yeast
    @PreUpdate
    public void onPreUpdate(){
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());  

        updated_at = currentTimestamp;
        edited = true;

        //filter newline characters
        comments = comments.replaceAll("\r\n|\n", "<br>");
    }



}
