##### Table of Contents  

- [Introduction](#introduction)
- [Preliminaries](#preliminaries)
- [Software installation](#software-installation)
- [Creating a new user and group](#Creating-a-new-user-and-group)
- [Directory organisation](#directory-organisation)
- [MySQL database](#mysql-database)
- [Apache2 web server](#apache2-web-server)
- [Execution](#execution)
- [Finalising](#finalising)
- [Copyright and License](#copyright-and-license)
- [About This Repository](#about-this-repository)


# Introduction

This software (`Strain Database`) has been successfully run on Ubuntu 24.04.4 LTS.  Earlier or later versions of Ubuntu may work, but they have not been tested.

Installation of this system has been broken down into the following steps:

1.  Software installation (Preliminary)
2.  Creating a new user and group
3.  Directory organisation
4.  MySQL Database
5.  Apache2 web server

You should have a background in Linux system administration to understand this document and have a system available which have [LAMP](https://en.wikipedia.org/wiki/LAMP_(software_bundle)) installed on it.  Installation and configuration on other systems (i.e., Microsoft Windows) is outside the scope of these instructions and this software has never been tested on it.


# Preliminaries

##  Notation

In this document, 

* Commands preceded by `$` are `bash` commands to be executed on the command line.
* Commands preceded by `mysql>` should be executed in the MySQL Monitor, which you can enter by running the following command:

```bash
mysql --user root --password
```

Exit the MySQL Monitor by using the `\q` command.  Setting the root password and/or creating an alternative account with administrative privileges for the MySQL Monitor is outside the scope of this document.

Variables (see next subsection) are enclosed in square brackets:  [].


##  Variables

This table summarises the variables whose values will be needed throughout this document.  Before you proceed, you should decide what values you will substitute the sample values with. Values that cannot be changed have been indicated.  Passwords, in particular, **must** be replaced.


| Variable                | Sample value                | Purpose                                                   |
| ----------------------- | --------------------------- | --------------------------------------------------------- | 
| [DB name]               | straindb                    | Name of the database (cannot be changed)                  |
| [DB username]           | straindb                    | Administrative user of the database                       |
| [DB password]           | itphIpdeph4on               | Administrative password of the database                   |
| [DB read only username] | perldb                      | User with database read-only access                       |
| [DB read only password] | Odak7DrapEam                | Password for user with database read-only access          |
| [install root]          | /straindb                   | Root directory of the system                              |
| [clone root]            | app                         | Location to clone this repository (within [install root]) |
| [PKCS12 password]       | pi&Floibs4                  | Password for creating the PKCS12 certificate              |
| [PKCS12 path]           | /etc/ssl/certs/keystore.p12 | Path to PKCS12 certificate                                |
| [IP address]            | 192.168.1.10                | IP address of the application                             |


##  Explanation of variables

The following describes how all of these variables fit together.  The application accesses the MySQL database called [DB name] using the administrative login [DB username] and the password [DB password].  When validating input values, a Perl script called `validate.pl` uses the read-only login [DB read only username] with a password of [DB read only password].  The application itself is installed in the directory [install root]/[clone root], where [clone root] is the directory where this repository has been cloned into from GitHub.  

The application will employ a certificate for encrypted traffic through the use of a certificate in privacy-enhanced mail (PEM) format.  The application requires this certificate to be in PKCS12 format.  The conversion from PEM to PKCS12 is encoded through the password [PKCS12 password].

The address of this application (and thus, the server on which it is running on) is https://[IP address]/


##  Inserting the variables

**After** [cloning the repository](#directory-organisation) (see below), you should make the following substitutions.

In `additional/compile-straindb.sh` and `additional/run-straindb.sh`, make the following substitutions:

| Bash variable          | Replacement variable        |
| ---------------------- | --------------------------- |
| db_username            | [DB username]               |
| db_password            | [DB password]               |
| main_path              | [install root]              |
| app_path               | [install root]/[clone root] |
| ssl_key_store_path     | [PKCS12 path]               |
| ssl_key_store_password | [PKCS12 password]           |
| application_domain     | https://[IP address]        |

In `additional/validate.pl`, make the following substitutions:

| Perl variable    | Replacement variable        |
| ---------------- | --------------------------- |
| $MYSQL_USER      | [DB read only username]     | 
| $MYSQL_PASSWORD  | [DB read only password]     | 


# Software installation

Software need to be installed via `apt-get` and through software download.


## Installation via `apt-get`

Install the following programs using `apt-get`:

```bash
## Install git
$ sudo apt-get install git

## Install dependencies for validate.pl
$ sudo apt-get install libdbd-mysql-perl libtext-csv-perl libtext-csv-xs-perl libappconfig-perl libappconfig-std-perl

## Install apache2 and MySQL
$ sudo apt-get install apache2 mysql-client mysql-server

## Install maven for compilation
$ sudo apt-get install maven
```


## Installation via download

Download Java from Amazon.  Both [Amazon Corretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html) and [Amazon Corretto 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html) have been tested successfully.  Assuming Amazon Corretto 21 has been selected, you can install it as follows:

```bash
$ wget https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.deb

$ sudo dpkg -i amazon-corretto-21-x64-linux-jdk.deb
```

The Linux x64 (JDK) Debian package (`.deb`) should be chosen.


## TLS certificate

A transport layer security (TLS) certificate is required for the correct installation of this system.  Consult your system administrator about obtaining and installing one, which should be done before you proceed.

For the purpose of these instructions, we will use the certificate in `/etc/ssl/certs/ssl-cert-snakeoil.pem`, which has not been signed by a public certificate authority (CA).  When deploying this system, this certificate should **NOT** be used!  One alternative is to use a certificate from [Let's Encrypt](https://letsencrypt.org/), whose set-up is beyond the scope of this document.



# Creating a new user and group

Create a new Ubuntu user and group.  We have chosen to use the name `straindb` for both, but you're welcome to user any name.

```bash
$ sudo adduser straindb
Adding user `straindb' ...
Adding new group `straindb' (1001) ...
Adding new user `straindb' (1001) with group `straindb' ...
Creating home directory `/home/straindb' ...
Copying files from `/etc/skel' ...
New password:
Retype new password:
passwd: password updated successfully
Changing the user information for straindb
Enter the new value, or press ENTER for the default
    Full Name []: Strain Database
    Room Number []:
    Work Phone []:
    Home Phone []:
    Other []:
Is the information correct? [Y/n] Y
```

Assign the current user (and any additional user that requires access) to this group:
```bash
## Add current user to the new group

$ sudo usermod -a -G straindb ${USER}
```

# Directory organisation

Create a directory on the server.  Any directory is fine; in the example below, we have chosen to use `/straindb`.  Take ownership of this directory and change directory to it.

```bash
$ sudo mkdir /straindb

$ sudo chown ${USER} /straindb

$ cd /straindb
```

## Cloning the GitHub repository

Create a private/public `ssh` key pair and install it into GitHub (see [here](https://docs.github.com/en/authentication/connecting-to-github-with-ssh) for more information.). Then, clone the repository into a directory called (`app`):
```bash
$ git clone git@github.com:rwanwork/straindb.git app
```


## Directory structure

After cloning the respository, numerous directories and symbolic links have to be created.  Assuming you are still in the (`/straindb`) directory, execute the following commands:

```bash
mkdir -p csv/csv
mkdir -p tmp upload
ln -s -f `pwd`/app/additional/validate.pl csv/csv/
ln -s -f `pwd`/app/additional/compile-straindb.sh .
ln -s -f `pwd`/app/additional/run-straindb.sh .
```

This will create the following directory structure:

```bash
.
├── app
├── csv
│   └── csv
├── tmp
└── upload
```


## Change directory permissions

We assign the `straindb` group to the entire directory and ensure this group also has write permissions:

```bash
$ cd /
$ sudo chgrp -R straindb /straindb
$ sudo chmod -R g+w /straindb
```


# MySQL Database

Either (1) an initial database has to be created or (2) a previously used database has to be restored.  Do not do both steps.

Regardless of which option is taken, two user accounts are created:
* straindb -- system administrator account
* perldb -- read-only account


## (1) Initialise the database

We initialise the database by first creating an empty one using the MySQL monitor:

```bash
$ mysql --user=root --password
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 16
Server version: 8.4.9-0ubuntu0.26.04.1 (Ubuntu)

Copyright (c) 2000, 2026, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> create database straindb;
Query OK, 1 row affected (0.02 sec)

mysql> \q
Bye
```

Then, we execute the SQL instructions to create an empty database:

```bash
mysql --user=root --password < app/additional/create-db.sql
```

An initial database has no administrative user within the application.  Therefore, we need to create one.  Note that the steps below should be done carefully and with only the first user!  Subsequent users within the application should be created by the administrator within the application (using a web browser).

Passwords of users created within the application will be stored encrypted automatically.  However, in this case, we are creating an initial user so we need to manually encrypt the password ourselves using the [Bcrypt Hash Generator](https://bcrypt.online/).  Go to this site and insert the password of your choice to get the encrypted string (which we will refer to as [encrypted password]).

Now, enter the MySQL monitor and run the following command, filling in the values in square brackets:

```bash
$ mysql --user root --password
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 13
Server version: 8.4.9-0ubuntu0.26.04.1 (Ubuntu)

Copyright (c) 2000, 2026, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> use straindb;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed

mysql> insert into users (initials, firstname, lastname, email, encrypted_password, current_sign_in_ip, last_sign_in_ip, admin, can_edit, approved, name, role
_id) values ("[initials]", "[first name]", "[last name]", "[e-mail address]", "[encrypted password]", "1.2.3.4", "1.2.3.4", 1, 1, 1, "[first name] [last name]", 2);
Query OK, 1 row affected (0.02 sec)
```

Note that the initials of the user are obtained from the first letters of the first and last names.  So, "Jane Doe" would be "JD".  The e-mail address is used by the user to login.  Also, the second to last value is the name of the user with the last name concatenated to the first name.  While it can differ from the `firstname` and `lastname` columns, we strongly encourage you to not do this.


## (2) Restore the database

Assuming the backup of the database is in the file `backup.sql`, we execute the following command:

```bash
mysql --user=root --password < backup.sql
```

## Create users

Within the MySQL monitor, we create two users.  One with full access to the Strain Database and another with just read-only access.  Provide different passwords for these two accounts in the `...` below:

```bash
mysql> create user 'straindb'@'localhost' identified with caching_sha2_password by '...';

mysql> create user 'perldb'@'localhost' identified with caching_sha2_password by '...';

mysql> grant all privileges on straindb.* to 'straindb'@'localhost' ;

mysql> grant select on straindb.* to 'perldb'@'localhost';

mysql> commit;
```

# Apache2 web server

This application will run on port 8080 by defaulit.  This is a port that is not normally open for incoming traffic.  As a consequence, we will forward traffic from port 80 (a port that is normally open) to port 8080 by:

1.  Configuring Apache2
2.  Enabling the firewall


## Configuring port forwarding

Add these two lines to the port 80 virtual host within `/etc/apache2/sites-enabled/000-default.conf`:
```bash
<VirtualHost *:80>
...

ProxyPreserveHost On

ProxyPass / http://localhost:8080/

</VirtualHost>
```

Enable these modules:
```bash
$ sudo a2enmod proxy

$ sudo a2enmod proxy_http
```

Finally, restart Apache2:
```bash 
$ sudo service apache2 restart
```

## Configuration of the firewall
We open up port 80 to the entire world and enable the firewall (in case it has not yet been activated):
```bash
## Open up port 80
$ sudo ufw allow to any port 80

$ sudo ufw enable
```

##  Configuring secure HTTP

We enable secure HTTP by doing the following:

1.  Creating the Apache2 configuration file.
2.  Converting the certificate.
3.  Updating the paths for the application.

### Creating the Apache2 configuration file

Edit (or create if it does not already exist) the file `/etc/apache2/sites-available/default-ssl.conf`.  Then, edit it so it has the following values:

```bash
    SSLEngine on

    SSLCertificateFile      /etc/ssl/certs/ssl-cert-snakeoil.pem
    SSLCertificateKeyFile   /etc/ssl/private/ssl-cert-snakeoil.key
    
    SSLProxyEngine on
    SSLProxyVerify none 
    SSLProxyCheckPeerCN off
    SSLProxyCheckPeerName off
    SSLProxyCheckPeerExpire off
    ProxyPreserveHost On
    ProxyPass / https://192.168.1.10:8443/
    ProxyPassReverse / https://192.168.1.10:8443/
```

If you have a certficate from a Certificate Authority, then change `SSLCertificateFile` and `SSLCertificateKeyFile`, accordingly.  Also, change the values in `ProxyPass` and `ProxyPassReverse` to [IP address].

Then, run the following commands to enable this configuration, as well as the Apache2 modules necessary.  The last command restarts the Apache2 web server.

```bash
sudo a2ensite default-ssl
sudo a2enmod ssl
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo service apache2 restart
```

### Converting the certificate

Convert the certificate in privacy-enhanced mail (PEM) format to PKCS12 (as required by the application) by executing this command:

```bash
sudo openssl pkcs12 -name tomcat -out /etc/ssl/certs/keystore.p12 -inkey /etc/ssl/private/ssl-cert-snakeoil.key -in /etc/ssl/certs/ssl-cert-snakeoil.pem  -export
```

Again, if you have a certificate from a Certificate Authority, change the paths in the above command accordingly.  You may also need to make use of the arguments `-CAfile` and `-caname`.  See this [web site](https://dzone.com/articles/spring-boot-secured-by-lets-encrypt) for some hints.


### Updating the paths for the application

In the application which you have cloned from GitHub, edit the files:

* `src/main/resources/application-test.properties`
* `src/main/resources/application-prod.properties`

so that the path is what you provided in the `openssl` command with the `-out` argument.


# Execution

We perform a test execution of the application before a live one, to make sure everything until this point is correct.

## Test execution of the application

We need to compile the application and then run it:

```bash
cd [install root]
./compile-straindb.sh
./run-straindb.sh
```

In the [install root] directory, both of these commands will produce log files with the `-compile.log` and `-run.log` endings.  If either have errors, then take a look at them for further information.

Note that the command `./run-straindb.sh` will not terminate.  In a web browser, go to https://[IP address]/ to see the application.

Assuming the system is correctly running, stop the execution of `./run-straindb.sh` by hitting CTRL + C.


## Live execution of the application

In order to execute the application, we need to make it run in the background.  An easy way to do that is to make it run as a service.

Ensure `[install root]/run-straindb.sh` has execute permissions.  Then, as system administrator, create the file `/etc/systemd/system/straindb.service` with the following contents:

```bash
[Unit]
Description=Strain Database
After=mysql.service

[Service]
ExecStart=[install root]/run-straindb.sh
Restart=on-failure
RestartSec=30

[Install]
WantedBy=multi-user.target
```

with [install root] being substituted with the correct value.  Change its permissions and verify that it is valid:

```bash
$ sudo chmod 664 /etc/systemd/system/straindb.service

$ sudo systemd-analyze verify /etc/systemd/system/straindb.service
```

Assuming there are no errors, reload `systemd` so that it is made aware of the new service. Then enable it:

```bash
$ sudo systemctl daemon-reload

$ sudo systemctl enable straindb.service

Created symlink /etc/systemd/system/multi-user.target.wants/straindb.service → /etc/systemd/system/straindb.service
```


# Finalising

Now that you have the system working, you might want to customise the site for your own laboratory.  To do that, follow the instructions in the accompanying `customisation.md` file.

After you have completed the changes, you will need to re-compile the system (see the steps above).  After re-compilation, you can re-run the service by typing `sudo service straindb restart`.


# Copyright and License


     Strain Database
     Copyright (C) 2024-2026, by the Cai Lab

Strain Database is distributed under the terms of the Apache License (Apache, version 2.0) -- see the file LICENSE for details.


# About This Repository

The software in this GitHub repository was developed by Research Software Engineers (RSEs) at the University of Manchester's Research IT team, as summarised [here](https://research-it.manchester.ac.uk/news/2026/05/12/revamping-the-strain-database-together/).  Development of this software was for the [Cai Lab](http://www.cailab.org/), also at the University of Manchester.

[Raymond Wan](https://github.com/rwanwork) has written this document as a member of Cai Lab.


