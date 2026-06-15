-- Copyright 2024-2026 The Cai Lab
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- ------------------------------------------------------------------------------
-- Select the database to use
-- ------------------------------------------------------------------------------
USE `straindb`;

-- ------------------------------------------------------------------------------
-- Tables with no dependencies
-- ------------------------------------------------------------------------------
create table bacterial_markers (
  id integer NOT NULL AUTO_INCREMENT,
  name VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  unique (name)
);


create table mating_types (
  id integer NOT NULL AUTO_INCREMENT,
  name VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  unique (name)
);


create table orientations (
  id integer NOT NULL AUTO_INCREMENT,
  name VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  unique (name)
);


create table yeast_markers (
  id integer NOT NULL AUTO_INCREMENT,
  name VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  unique (name)
);


create table user_role (
  id integer NOT NULL AUTO_INCREMENT,
  role VARCHAR (255) NOT NULL,
  primary key (id),
  unique (role)
);


-- ------------------------------------------------------------------------------
-- Tables with no dependencies
-- ------------------------------------------------------------------------------
create table mammalians_species (
  id integer NOT NULL AUTO_INCREMENT,
  ncbi_id integer NOT NULL,
  scientific_name VARCHAR (255) NOT NULL,
  common_name VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  unique (ncbi_id),
  unique (scientific_name)
);

create table plants_species (
  id integer NOT NULL AUTO_INCREMENT,
  ncbi_id integer NOT NULL,
  scientific_name VARCHAR (255) NOT NULL,
  common_name VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  unique (ncbi_id),
  unique (scientific_name)
);


-- ------------------------------------------------------------------------------
-- Table with dependencies on user_role
-- ------------------------------------------------------------------------------
create table users (
  id integer NOT NULL AUTO_INCREMENT,
  deactivate tinyint (1) DEFAULT 0,
  initials varchar (255) DEFAULT NULL,
  firstname varchar (255) NOT NULL,
  lastname varchar (255) NOT NULL,
  email VARCHAR (255) NOT NULL,
  encrypted_password VARCHAR (255) NOT NULL,
  reset_password_token VARCHAR (255),
  reset_password_sent_at timestamp DEFAULT NULL,
  sign_in_count integer DEFAULT 0 NOT NULL,
  current_sign_in_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_sign_in_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  current_sign_in_ip VARCHAR (255) NOT NULL,
  last_sign_in_ip VARCHAR (255) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  admin boolean NOT NULL DEFAULT 0,
  can_edit boolean NOT NULL DEFAULT 0,
  approved boolean NOT NULL DEFAULT 0,
  name VARCHAR (255) NOT NULL,
  role_id integer NOT NULL DEFAULT 1,
  primary key (id),
  unique (initials),
  unique (email),
  foreign key (role_id) references user_role (id) on delete cascade
);


-- ------------------------------------------------------------------------------
-- Tables with dependencies on users
-- ------------------------------------------------------------------------------
create table bacteria (
  id integer NOT NULL AUTO_INCREMENT,
  user_id integer,
  plasmid_name VARCHAR (255),
  alternate_name VARCHAR (255),
  genotype VARCHAR (255),
  host_strain VARCHAR (255),
  comments text,
  location VARCHAR (255),
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  bacterial_marker_id integer,
  other_bacterial_marker VARCHAR (255),
  lab_id VARCHAR (255) NOT NULL,
  personal_id VARCHAR (255) NOT NULL,
  edited boolean NOT NULL DEFAULT 0,
  attachment_file_name VARCHAR (255),
  attachment_content_type VARCHAR (255),
  attachment_file_size integer,
  attachment_updated_at timestamp,
  plasmid_data json,
  soft_delete boolean NOT NULL DEFAULT 0,
  primary key (id),
  unique (lab_id),
  unique (personal_id),
  foreign key (user_id) references users (id) on delete cascade,
  foreign key (bacterial_marker_id) references bacterial_markers (id) on delete cascade
);


create table primers (
  id integer NOT NULL AUTO_INCREMENT,
  description text NOT NULL,
  sequence text NOT NULL,
  melting_temperature integer,
  concentration VARCHAR (255),
  vendor VARCHAR (255),
  location VARCHAR (255),
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comments text,
  user_id integer,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lab_id VARCHAR (255) NOT NULL,
  personal_id VARCHAR (255) NOT NULL,
  orientation_id integer,
  edited boolean NOT NULL DEFAULT 0,
  plate_id integer,
  well_id VARCHAR (255),
  soft_delete boolean NOT NULL DEFAULT 0,
  primary key (id),
  unique (lab_id),
  unique (personal_id),
  foreign key (user_id) references users (id) on delete cascade,
  foreign key (orientation_id) references orientations (id) on delete cascade
);


create table yeasts (
  id integer NOT NULL AUTO_INCREMENT,
  user_id integer,
  other_names VARCHAR (255),
  plasmid VARCHAR (255),
  genotype VARCHAR (255) NOT NULL,
  plasmid_type VARCHAR (255),
  marker VARCHAR (255),
  location VARCHAR (255),
  comments text,
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  parent_id integer,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  other_mating_type VARCHAR (255),
  mating_type_id integer,
  other_yeast_marker_id VARCHAR (255),
  lab_id VARCHAR (255) NOT NULL,
  personal_id VARCHAR (255) NOT NULL,
  edited boolean NOT NULL DEFAULT 0,
  parent_name VARCHAR (255),
  parent_name1 VARCHAR (255) DEFAULT NULL,
  parent_name2 VARCHAR (255) DEFAULT NULL,
  markers_list VARCHAR (255) DEFAULT NULL,
  primary key (id),
  unique (lab_id),
  unique (personal_id),
  foreign key (user_id) references users (id) on delete cascade,
  foreign key (mating_type_id) references mating_types (id) on delete cascade
);

create table mammalians (
  id integer NOT NULL AUTO_INCREMENT,
  personal_id VARCHAR (255) NOT NULL,
  lab_id VARCHAR (255) NOT NULL,
  name VARCHAR (255),
  user_id integer,
  cell_line VARCHAR (255) NOT NULL DEFAULT ("-"),
  passage_number integer NOT NULL DEFAULT 0,
  cell_type VARCHAR (255),
  karyotype VARCHAR (255),
  genotype VARCHAR (255),
  source VARCHAR (255),
  marker VARCHAR (255),
  media VARCHAR (255) NOT NULL DEFAULT ("-"),
  species integer NOT NULL DEFAULT 0,
  date_frozen timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comments text,
  location VARCHAR (255),
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edited boolean NOT NULL DEFAULT 0,
  primary key (id),
  unique (lab_id),
  unique (personal_id),
  foreign key (user_id) references users (id) on delete cascade,
  foreign key (species) references mammalians_species (ncbi_id) on delete cascade
);

create table plants (
  id integer NOT NULL AUTO_INCREMENT,
  personal_id VARCHAR (255) NOT NULL,
  lab_id VARCHAR (255) NOT NULL,
  name VARCHAR (255),
  user_id integer,
  growth_conditions VARCHAR (255),
  genotype VARCHAR (255) NOT NULL DEFAULT ("-"),
  source VARCHAR (255),
  marker VARCHAR (255),
  media VARCHAR (255),
  species integer NOT NULL DEFAULT 0,
  comments text,
  location VARCHAR (255),
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edited boolean NOT NULL DEFAULT 0,
  primary key (id),
  unique (lab_id),
  unique (personal_id),
  foreign key (user_id) references users (id) on delete cascade,
  foreign key (species) references plants_species (ncbi_id) on delete cascade
);


-- ------------------------------------------------------------------------------
-- Tables with dependencies on yeasts and yeast_markers
-- ------------------------------------------------------------------------------
create table yeast_is_marked_bies (
  id integer NOT NULL AUTO_INCREMENT,
  yeast_marker_id integer NOT NULL,
  yeast_id integer NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key (id),
  foreign key (yeast_marker_id) references yeast_markers (id) on delete cascade
);


-- ------------------------------------------------------------------------------
-- Insert some records
-- ------------------------------------------------------------------------------
INSERT INTO user_role (id, role) VALUES (1, 'Normal user');
INSERT INTO user_role (id, role) VALUES (2, 'Administrator');

INSERT INTO mammalians_species (ncbi_id, scientific_name, common_name) VALUES (9606, 'Homo sapiens', 'human');
INSERT INTO mammalians_species (ncbi_id, scientific_name, common_name) VALUES (10090, 'Mus musculus', 'house mouse');

INSERT INTO plants_species (ncbi_id, scientific_name, common_name) VALUES (3702, 'Arabidopsis thaliana', 'thale cress');
INSERT INTO plants_species (ncbi_id, scientific_name, common_name) VALUES (4113, 'Solanum tuberosum', 'potato');
INSERT INTO plants_species (ncbi_id, scientific_name, common_name) VALUES (4120, 'Ipomoea batatas', 'sweet potato');

