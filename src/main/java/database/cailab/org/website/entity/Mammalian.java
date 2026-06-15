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

import database.cailab.org.website.service.ApplicationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mammalians")
public class Mammalian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Personal ID is too long, please try again.")
    private String personal_id;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Lab ID is too long, please try again.")
    private String lab_id;

    @Column(length = 255)
    @Size(max = 255, message = "Name is too long, please try again.")
    private String name;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false, length = 255)
    @NotEmpty(message = "Cell Line cannot be blank")
    @Size(max = 255, message = "Cell Line is too long, please try again.")
    private String cell_line;

    @Column(nullable = false)
    @NotNull(message = "Passage Number cannot be blank")
    private Integer passage_number;

    @Column(length = 255)
    @Size(max = 255, message = "Cell type is too long, please try again.")
    private String cell_type;

    @Column(length = 255)
    @Size(max = 255, message = "Karyotype is too long, please try again.")
    private String karyotype;

    @Column(length = 255)
    @Size(max = 255, message = "Genotype is too long, please try again.")
    private String genotype;

    @Column(length = 255)
    @Size(max = 255, message = "Source is too long, please try again.")
    private String source;

    @Column(length = 255)
    @Size(max = 255, message = "Marker is too long, please try again.")
    private String marker;

    @Column(nullable = false, length = 255)
    @NotEmpty(message = "Media cannot be blank")
    @Size(max = 255, message = "Media is too long, please try again.")
    private String media;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "species", referencedColumnName="ncbi_id")
    private MammalianSpecies mammalianSpecies;

    @Column(nullable = false, name = "date_frozen")
    private Timestamp datefrozen;

    private String comments;

    @Column(length = 255)
    @Size(max = 255, message = "Location is too long, please try again.")
    private String location;

    @Column(nullable = false)
    private Timestamp date;

    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;

    @Column(nullable = false)
    private Boolean edited;

    // set default value and data transformation before new mammalian insert into table
    @PrePersist
    public void onCreate() {
        Timestamp currentTimestamp = ApplicationUtils.getCurrentTimestamp();
        
        created_at = currentTimestamp;
        updated_at = currentTimestamp;
        edited = false; // default value for edited

        // Change new line characters to <br> in comments
        if (comments != null) {
            comments = ApplicationUtils.ConvertNewLineToBr(comments);
        }
    }

    // set default value and data transformation before update mammalian
    @PreUpdate
    public void onPreUpdate() {
        Timestamp currentTimestamp = ApplicationUtils.getCurrentTimestamp();
        
        updated_at = currentTimestamp;
        edited = true; // set edited to true on update

        // Change new line characters to <br> in comments
        if (comments != null) {
            comments = ApplicationUtils.ConvertNewLineToBr(comments);
        }
    }

}
