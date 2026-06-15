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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "users")
public class Users implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 255, unique = true)
    @Email
    @NotBlank
    @NotEmpty
    @Size(max = 255, message = "Too long, try again.")
    private String email;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String encrypted_password;

    @Column(length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String reset_password_token;

    private Timestamp reset_password_sent_at;

    @Column(nullable = false)
    private Integer sign_in_count;

    @Column(nullable = false)
    private Timestamp current_sign_in_at;

    @Column(nullable = false)
    private Timestamp last_sign_in_at;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String current_sign_in_ip;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String last_sign_in_ip;

    @Column(nullable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;

    @Column(nullable = false)
    private Boolean admin;

    @Column(nullable = false)
    private Boolean can_edit;

    @Column(nullable = false)
    private Boolean approved;

    @Column(nullable = false, length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String name;

    private Boolean deactivate;

    @Column(length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String initials;

    @NotBlank
    @NotEmpty
    @Column(length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String firstname;

    @NotBlank
    @NotEmpty
    @Column(length = 255)
    @Size(max = 255, message = "Too long, try again.")
    private String lastname;

    // relationship
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bacteria> bacteria;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Primers> primers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Yeast> yeast;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mammalian> mammalian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private UserRole userRole;

    // set default value and data transformation before new User insert into table
    @PrePersist
    public void onCreate() {
        Date currentDate = new Date();
        // Create a Timestamp from the current date
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

        deactivate = false;
        sign_in_count = 0;
        current_sign_in_at = currentTimestamp;
        last_sign_in_at = currentTimestamp;
        created_at = currentTimestamp;
        updated_at = currentTimestamp;

        if (admin == null) {
            admin = false;
        }

        if (can_edit == null) {
            can_edit = true;
        }

        if (approved == null) {
            approved = false;
        }

        if (userRole == null || userRole.getId() == null) {
            userRole = new UserRole();
            userRole.setId(1);
        }

        // remove email white space
        email = email.trim();

        // upper case of the first letter of the first name and last name
        // first name may contain middle name
        // firstname = Arrays.stream(firstname.split(" ")).map(
        //         s -> s.substring(0, 1).toUpperCase() + s.substring(1))
        //         .collect(Collectors.joining(" "));

        // lastname = lastname.substring(0, 1).toUpperCase() + lastname.substring(1);

        name = String.format("%s %s", firstname, lastname);

    }

    // for authenication
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (admin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (can_edit) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EDITOR"));
        }

        if (approved) {
            authorities.add(new SimpleGrantedAuthority("ROLE_APPROVER"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.encrypted_password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        /*
         * Deactivate value:
         * 1 (true) -> account disable = account enable should return false
         * 0 (false) -> account enable = account enable true
         */
        return !this.getDeactivate();
    }
}
