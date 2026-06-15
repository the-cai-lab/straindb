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
import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
@Table(name = "bacteria")
public class Bacteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

//    private Integer user_id;

    @Column(length = 255)
    @Size(max = 255, message = "Plasmld name is too long, please try again.")
    private String plasmid_name;

    @Column(length = 255)
    @Size(max = 255, message = "Genotype is too long, please try again.")
    private String genotype;

    @Column(length = 255)
    @Size(max = 255, message = "Alternate name is too long, please try again.")
    private String alternate_name;

    @Column(length = 255)
    @Size(max = 255, message = "Host Strain is too long, please try again.")
    private String host_strain;

    private String comments;

    @Column(length = 255)
    @Size(max = 255, message = "Location is too long, please try again.")
    private String location;

    private Timestamp date;

    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;

    // private Integer bacterial_marker_id;

    @Column(length = 255)
    @Size(max = 255, message = "Other bacterial marker is too long, please try again.")
    private String other_bacterial_marker;

    @Column(length = 255)
    @Size(max = 255, message = "Lab ID is too long, please try again.")
    private String lab_id;

    @Column(length = 255)
    @Size(max = 255, message = "Personal ID is too long, please try again.")
    private String personal_id;

    private Boolean edited;

    @Column(length = 255)
    @Size(max = 255, message = "Attachment file name is too long, please try again.")
    private String attachment_file_name;

    @Column(length = 255)
    @Size(max = 255, message = "Attachment content type is too long, please try again.")
    private String attachment_content_type;

    private Long attachment_file_size;

    private Timestamp attachment_updated_at;

    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private String plasmid_data;

    private Boolean soft_delete;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bacterial_marker_id")
    private BacterialMarkers bacterialMarkers;

    // set default value and data transformation before new bacteria insert into table
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
        comments = comments.replaceAll("\r\n|\n", "<br>");
    }

    //set default value and data transformation before update bacteria
    @PreUpdate
    public void onPreUpdate() {
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
        
        updated_at = currentTimestamp;
        edited = true;
        
        //filter newline characters
        comments = comments.replaceAll("\r\n|\n", "<br>");
    }

}
