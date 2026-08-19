#!/usr/bin/env perl
##
## Copyright 2024-2026 The Cai Lab
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##


use FindBin;
use lib $FindBin::Bin;  ##  Search the directory where the script is located

use diagnostics;
use strict;
use warnings;

##  Module for handling arguments
use AppConfig;
use AppConfig::Getopt;

##  Module for providing documentation
use Pod::Usage;

##  Module for accessing MySQL
use DBD::mysql;

##  Module for reading/writing CSV files
use Text::CSV;

##  Module for checking dates
use Date::Manip::Date;


########################################
##  Important variables
########################################

##  Variables for command-line arguments
my $input_arg = "";
my $output_arg = "";
my $datatype_arg = "";
my $report_arg = "";

##  The report that will be shown to the user
my $report_str = "";

##  Temporary variables
my $tmp_str = "";

##  Set the return value
my $EXIT_SUCCESS = 0;
my $EXIT_FAILURE = 1;
my $program_return_value = $EXIT_SUCCESS;

##  Database handler
my $db_handler;

##  Array of records
my @records;

##  Hash of database records:  i.e. $db{table name}{variable name} = unique identifier
my %db;

##  Record of tables that we have downloaded
my %db_tables;

my %db_options_str;

##  The maximum length for the set of DB options.
my $MAX_DB_OPTIONS_LENGTH = 64;

my $MYSQL_USER = "perldb";
my $MYSQL_PASSWORD = "Odak7DrapEam";


########################################
##  Configuration of the four types of tables
########################################

##  Define the number of columns for each table
my %num_columns;
$num_columns{bacteria} = 8;
$num_columns{mammalians} = 13;
$num_columns{plants} = 9;
$num_columns{primers} = 10;
$num_columns{yeasts} = 11;

##  Possibly values are:
##    * varchar
##    * integer
##    * date (Date datatype in YYYY-MM-DD or DD-MM-YYYY format)
##    * DB.<table name>.<column name>
##    * DBsp.<table name>.<column name> -- value of "column name" is not used and, thus, doesn't matter
##
##  Each of these can end with a "0".  If so, this means that it is required in the table (i.e., not NULL).
##
##  By default, the row's (numerical) primary key is returned.  If they end with "STR", then the value of the variable is returned instead.
my %datatypes;
$datatypes{bacteria} = [ ("varchar", "varchar", "varchar", "varchar", "DB.bacterial_markers.name", "varchar", "varchar", "varchar") ];
$datatypes{mammalians} = [ ("varchar", "varchar-0", "integer-0", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar-0", "DBsp.mammalians_species.all-0", "varchar", "date-0", "varchar") ];
$datatypes{plants} = [ ("varchar", "varchar", "varchar-0", "varchar", "varchar", "varchar", "DBsp.plants_species.all-0", "varchar", "varchar") ];
$datatypes{primers} = [ ("varchar-0", "varchar-0", "integer", "varchar", "varchar", "DB.orientations.name", "integer", "varchar", "varchar", "varchar") ];
$datatypes{yeasts} = [ ("varchar-0", "varchar", "varchar", "varchar", "DB.mating_types.name", "varchar", "DB.yeasts.personal_id-STR", "DB.yeasts.personal_id-STR", "varchar", "varchar", "varchar") ];


########################################
##  Subroutines
########################################

##  Substitute each tab character with a single space
sub substituteTabs {
  my ($field) = @_;

  $field =~ s/\t/ /gs;

  return ($field);
}


##  Remove whitespaces at the beginning or end of a field
sub trimOuterSpaces {
  my ($field) = @_;

#  printf STDERR "[%s]\t", $field;

  if ($field =~ /^\s+(\S)(.*)$/) {
    $field = $1.$2;
  }
  if ($field =~ /^(.*)(\S)\s+$/) {
    $field = $1.$2;
  }
  if ($field =~ /^(\s+)$/) {
    $field = "";
  }

#  printf STDERR "[%s]\n", $field;

  return ($field);
}


##  Fix up a varchar by substituting tabs and then removing whitespaces
##  at the beginning and end
sub fixVarchar {
  my ($field) = @_;

  $field = substituteTabs ($field);
  $field = trimOuterSpaces ($field);

  return ($field);
}


##  Search the cache database records for a particular variable, returing its corresponding primary key
sub searchDatabaseID {
  my ($mysql_table, $mysql_var, $old_value) = @_;

  my $return_value = $EXIT_SUCCESS;
  my $message = "";

  my $new_value = "";

  ##  All variables are assumed to be in lower case for the string match
  $old_value = lc ($old_value);

  if (defined ($db{$mysql_table}{$old_value})) {
    $new_value = $db{$mysql_table}{$old_value};
  }
  else {
    $return_value = $EXIT_FAILURE;
    $message = "is not valid.  Please choose from [".$db_options_str{$mysql_table}."].  Contact the system administrator if an option you would like is not available.";
  }

  return ($return_value."\t".$new_value."\t".$message);
}


##  Search the cache database records for a particular variable, returning it
sub searchDatabaseName {
  my ($mysql_table, $mysql_var, $old_value) = @_;

  my $return_value = $EXIT_SUCCESS;
  my $message = "";

  my $new_value = "";

  if (defined ($db{$mysql_table}{$old_value})) {
    $new_value = $old_value;
  }
  else {
    $old_value = lc ($old_value);
    if (defined ($db{$mysql_table}{$old_value})) {
      $return_value = $EXIT_FAILURE;
      $message = "is not valid.  But, a similar value exists (".$old_value.").  Note that data entry for this field is case sensitive.";
    }
    else {
      $return_value = $EXIT_FAILURE;
      if (length ($db_options_str{$mysql_table}) > $MAX_DB_OPTIONS_LENGTH) {
        $message = "is not valid.  Please double-check your input or contact the system administrator for additional help.";      
      }
      else {
        $message = "is not valid.  Please choose from [".$db_options_str{$mysql_table}."].  Contact the system administrator if an option you would like is not available.";
      }
    }
  }

  return ($return_value."\t".$new_value."\t".$message);
}


##  Check a value, when given a data type
##  Return value:  ($return_value, $new_value, $message)
sub checkValue {
  my ($value, $datatype) = @_;

  my $return_value = $EXIT_SUCCESS;
  my $message = "";

  my $old_value = $value;
  my $new_value = "";

  ##  The returned data structure has three parts
  my @return_array = ();

  if ($datatype =~ /^varchar/) {
    $new_value = fixVarchar ($old_value);

    ##  Value is mandatory, but no value is provided
    if (($datatype =~ /0$/) && (length ($new_value) == 0)) {
      $return_value = $EXIT_FAILURE;
      $message = "is required but no value was provided.";
    }
    else {
      $return_value = $EXIT_SUCCESS;
      if ($new_value eq $old_value) {
        $message = "";
      }
      else {
        $message = "has been cleaned up.";
      }
    }
  }
  elsif ($datatype =~ /^integer/) {
    $new_value = trimOuterSpaces ($old_value);

    if (length ($new_value) == 0) {
      if ($datatype =~ /0$/) {
        $return_value = $EXIT_FAILURE;
        $message = "is required but no value was provided.";
      }
      else {
        $return_value = $EXIT_SUCCESS;
        $message = "";
      }
    }
    elsif ($new_value !~ /^\d+$/) {
      $return_value = $EXIT_FAILURE;
      $new_value = "";
      $message = "is not a valid number.";
    }
    else {
      $return_value = $EXIT_SUCCESS;
      $message = "";
    }
  }
  elsif ($datatype =~ /^date/) {
    $new_value = trimOuterSpaces ($old_value);

    if (length ($new_value) == 0) {
      if ($datatype =~ /0$/) {
        $return_value = $EXIT_FAILURE;
        $message = "is required but no value was provided.";
      }
      else {
        $return_value = $EXIT_SUCCESS;
        $message = "";
      }
    }

    ##  If the date cannot be parsed, then this has a non-zero value
    my $parse_error = 0;

    my $year = "";
    my $month = "";
    my $day = "";

    my $date = new Date::Manip::Date;

    ##  If the date is in a DD-MM-YYYY format, then flip it to YYYY-MM-DD format
    if ($new_value =~ /^(\d\d)-(\d\d)-(\d\d\d\d)$/) {
      $day = $1;
      $month = $2;
      $year = $3;
      if (($month <= 12) && ($day <= 31)) {
        $new_value = $year."-".$month."-".$day;
      }
    }

    ##  With the date in YYYY-MM-DD format, check it
    if ($new_value =~ /^(\d\d\d\d)-(\d\d)-(\d\d)$/) {
      $year = $1;
      $month = $2;
      $day = $3;

      $parse_error = $date -> parse_format ('%Y-%m-%d', $new_value);
    }
    else {
      $parse_error = 1;
    }

    if ($parse_error == 1) {
      $return_value = $EXIT_FAILURE;
      $new_value = "";
      $message = "is not a valid date.  Please use the format YYYY-MM-DD.";
    }
  }
  elsif ($datatype =~ /^DB\.([^\.]+)\.([^\.]+)$/) {
    my $mysql_table = $1;
    my $mysql_var = $2;
    
    my $is_required = 0;
    my $return_name = 0;
    
    if ($mysql_var =~ /^(.+)-STR-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-STR$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 0;
    }
    elsif ($mysql_var =~ /^(.+)$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 0;
    }
    
    $new_value = trimOuterSpaces ($old_value);

    if (length ($new_value) == 0) {
      if ($is_required == 1) {
        $return_value = $EXIT_FAILURE;
        $message = "is required but no value was provided.";
      }
      elsif ($is_required == 0) {
        $return_value = $EXIT_SUCCESS;

        if ($new_value eq $old_value) {
          $message = "";
        }
        else {
          $message = "has been cleaned up.";
        }
      }
    }
    else {
      my $database_result;
      
      if ($return_name == 1) {
        $database_result = searchDatabaseName ($mysql_table, $mysql_var, $new_value);
        ($return_value, $new_value, $message) = split /\t/, $database_result;
      }
      else {
        $database_result = searchDatabaseID ($mysql_table, $mysql_var, $new_value);
        ($return_value, $new_value, $message) = split /\t/, $database_result;
      }      
    }
  }
  elsif ($datatype =~ /^DBsp\.([^\.]+)\.([^\.]+)$/) {
    my $mysql_table = $1;
    my $mysql_var = $2;

    my $is_required = 0;
    my $return_name = 0;

    if ($mysql_var =~ /^(.+)-STR-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-STR$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 0;
    }
    elsif ($mysql_var =~ /^(.+)$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 0;
    }

    $new_value = trimOuterSpaces ($old_value);

    if (length ($new_value) == 0) {
      if ($is_required == 1) {
        $return_value = $EXIT_FAILURE;
        $message = "is required but no value was provided.";
      }
      elsif ($is_required == 0) {
        $return_value = $EXIT_SUCCESS;

        if ($new_value eq $old_value) {
          $message = "";
        }
        else {
          $message = "has been cleaned up.";
        }
      }
    }
    else {
      my $database_result;

      if ($return_name == 1) {
        $database_result = searchDatabaseName ($mysql_table, $mysql_var, $new_value);
        ($return_value, $new_value, $message) = split /\t/, $database_result;
      }
      else {
        $database_result = searchDatabaseID ($mysql_table, $mysql_var, $new_value);
        ($return_value, $new_value, $message) = split /\t/, $database_result;
      }
    }
  }
  else {
    printf STDERR "EE\tUnknown attribute type [%s]!\n", $datatype;
    exit ($EXIT_FAILURE);
  }

  ##  Pack the three parts into an array
  push (@return_array, $return_value);
  push (@return_array, $new_value);
  push (@return_array, $message);

  ##  Return the array
  return (@return_array);
}


########################################
##  Check global variables; problems here are due to a development 
##    bug (i.e., not from user input)
########################################

##  The %datatypes and %num_columns need to match
foreach my $key (sort (keys %datatypes)) {
  if (scalar (@{ $datatypes{$key} }) != $num_columns{$key}) {
    printf STDERR "EE\tMismatch in datatype sizes for %s (%u != %u)!\n", $key, scalar (@{ $datatypes{$key} }), $num_columns{$key};
    exit ($EXIT_FAILURE);
  }
}


########################################
##  Process arguments
########################################

##  Create AppConfig and AppConfig::Getopt objects
my $config = AppConfig -> new ({
  GLOBAL => {
    DEFAULT => undef,      ##  Default value for new variables
  }
});

my $getopt = AppConfig::Getopt -> new ($config);

##  General program options
$config -> define ("verbose", {
  DEFAULT  => 0,
  ARGCOUNT => AppConfig::ARGCOUNT_ONE,
  ARGS => "=i"
});                        ##  Verbose output

$config -> define ("help!", {
  ARGCOUNT => AppConfig::ARGCOUNT_NONE
});                        ##  Help screen

$config -> define ("debug!", {
  ARGCOUNT => AppConfig::ARGCOUNT_NONE
});                        ##  Debug messages

##  Program parameters
$config -> define ("input", {
  ARGCOUNT => AppConfig::ARGCOUNT_ONE,
  ARGS => "=s"
});                        ##  Input file

$config -> define ("output", {
  ARGCOUNT => AppConfig::ARGCOUNT_ONE,
  ARGS => "=s"
});                        ##  Output file

$config -> define ("datatype", {
  ARGCOUNT => AppConfig::ARGCOUNT_ONE,
  ARGS => "=s"
});                        ##  Type of data

$config -> define ("report", {
  ARGCOUNT => AppConfig::ARGCOUNT_ONE,
  ARGS => "=s"
});                        ##  Output report

##  Process the command-line options
$config -> getopt ();


########################################
##  Validate the settings
########################################

if ($config -> get ("help")) {
  pod2usage (-verbose => 0);
  exit ($EXIT_FAILURE);
}

if (!defined ($config -> get ("input"))) {
  printf STDERR "EE\tThe option --input requires a filename.\n";
  exit ($EXIT_FAILURE);
}
$input_arg = $config -> get ("input");


if (!defined ($config -> get ("output"))) {
  printf STDERR "EE\tThe option --output requires a filename.\n";
  exit ($EXIT_FAILURE);
}
$output_arg = $config -> get ("output");


if (!defined ($config -> get ("datatype"))) {
  printf STDERR "EE\tThe option --datatype requires one of bacteria, mammalians, plants, primers, or yeasts.\n";
  exit ($EXIT_FAILURE);
}
$datatype_arg = $config -> get ("datatype");

if (($datatype_arg ne "bacteria") && ($datatype_arg ne "mammalians") && ($datatype_arg ne "plants") && ($datatype_arg ne "primers") && ($datatype_arg ne "yeasts")) {
  printf STDERR "EE\tThe option --datatype requires one of bacteria, mammalians, plants, primers, or yeasts.\n";
  exit ($EXIT_FAILURE);
}

if (!defined ($config -> get ("report"))) {
  printf STDERR "EE\tThe option --report requires an output filename.\n";
  exit ($EXIT_FAILURE);
}
$report_arg = $config -> get ("report");


########################################
##  Connect to the database and access the records, based on the database
########################################

##  See:  https://www.perltutorial.org/perl-dbi/
$db_handler = DBI -> connect ("DBI:mysql:straindb", $MYSQL_USER, $MYSQL_PASSWORD);
if (!$db_handler) {
  die "EE\tFailed to connect to MySQL database DBI -> errstr ()\n";
}
else {
  if ($config -> get ("debug")) {
    printf STDERR "II\tConnected to MySQL server successfully.\n";
  }
}

for (my $m = 0; $m < scalar ($num_columns{$datatype_arg}); $m++) {
  my $mysql_table = "";
  my $mysql_var = "";

  if ($datatypes{$datatype_arg}[$m] =~ /^DB\.([^\.]+)\.([^\.]+)$/) {
  
    $mysql_table = $1;
    $mysql_var = $2;

    ##  Check if we've processed this table already
    if ((defined ($db_options_str{$mysql_table})) && (length ($db_options_str{$mysql_table}) > 0)) {
      next;
    }
  
    my $is_required = 0;
    my $return_name = 0;

    if ($mysql_var =~ /^(.+)-STR-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-STR$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 0;
    }
    elsif ($mysql_var =~ /^(.+)$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 0;
    }

    if ($config -> get ("debug")) {
      printf STDERR "II\tTable:  [%s]\n", $mysql_table;
      printf STDERR "II\tVariable:  [%s]\n", $mysql_var;
    }

    ##  Prepare SQL statement
    my $mysql_query_str = sprintf ("SELECT id, %s FROM %s ORDER BY id", $mysql_var, $mysql_table);
    if ($config -> get ("debug")) {
      printf STDERR "II\tExecuting SQL query:  %s\n", $mysql_query_str;
    }
    my $sth = $db_handler -> prepare ($mysql_query_str) or die "EE\tPrepare statement failed: $db_handler -> errstr ()\n";

    $sth -> execute() or die "EE\tExecution failed:  $db_handler -> errstr ()\n";

    my $mysql_input_id = "";
    my $mysql_input_var = "";

    ##  Loop through each row of the result set, and print it
    while(($mysql_input_id, $mysql_input_var) = $sth -> fetchrow ()) {
      if ($return_name == 0) {
        $mysql_input_var = lc ($mysql_input_var);
      }
      
      if ($config -> get ("debug")) {
        printf STDERR "II\t%s\t%s\n", $mysql_input_id, $mysql_input_var;
      }
      $db{$mysql_table}{$mysql_input_var} = $mysql_input_id;

      ##  Create a string with the list of available options, which can be used as a message to the user
      if (!defined ($db_options_str{$mysql_table})) {
        $db_options_str{$mysql_table} = $mysql_input_var;
      }
      else {
        $db_options_str{$mysql_table} = $db_options_str{$mysql_table}.", ".$mysql_input_var;
      }
    }

    ##  Close the fetch
    $sth -> finish();    
  }
  elsif ($datatypes{$datatype_arg}[$m] =~ /^DBsp\.([^\.]+)\.([^\.]+)$/) {
    $mysql_table = $1;
    $mysql_var = $2;

    ##  Check if we've processed this table already
    if ((defined ($db_options_str{$mysql_table})) && (length ($db_options_str{$mysql_table}) > 0)) {
      next;
    }

    my $is_required = 0;
    my $return_name = 0;

    if ($mysql_var =~ /^(.+)-STR-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-STR$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 1;
    }
    elsif ($mysql_var =~ /^(.+)-0$/) {
      $mysql_var = $1;
      $is_required = 1;
      $return_name = 0;
    }
    elsif ($mysql_var =~ /^(.+)$/) {
      $mysql_var = $1;
      $is_required = 0;
      $return_name = 0;
    }

    if ($config -> get ("debug")) {
      printf STDERR "II\tTable:  [%s]\n", $mysql_table;
    }

    ##  Prepare SQL statement
    my $mysql_query_str = sprintf ("SELECT id, ncbi_id, scientific_name, common_name FROM %s ORDER BY id", $mysql_table);
    if ($config -> get ("debug")) {
      printf STDERR "II\tExecuting SQL query:  %s\n", $mysql_query_str;
    }
    my $sth = $db_handler -> prepare ($mysql_query_str) or die "EE\tPrepare statement failed: $db_handler -> errstr ()\n";

    $sth -> execute() or die "EE\tExecution failed:  $db_handler -> errstr ()\n";

    my $mysql_input_id = "";
    my $mysql_input_ncbi_id = "";
    my $mysql_input_scientific_name = "";
    my $mysql_input_common_name = "";

    ##  Loop through each row of the result set, and print it
    while(($mysql_input_id, $mysql_input_ncbi_id, $mysql_input_scientific_name, $mysql_input_common_name) = $sth -> fetchrow ()) {
      if ($return_name == 0) {
        $mysql_input_ncbi_id = lc ($mysql_input_ncbi_id);
        $mysql_input_scientific_name = lc ($mysql_input_scientific_name);
        $mysql_input_common_name = lc ($mysql_input_common_name);
      }

      if ($config -> get ("debug")) {
        printf STDERR "II\t%s\t%s\t%s\t%s\n", $mysql_input_id, $mysql_input_ncbi_id, $mysql_input_scientific_name, $mysql_input_common_name;
      }

      $db{$mysql_table}{$mysql_input_ncbi_id} = $mysql_input_ncbi_id;
      $db{$mysql_table}{$mysql_input_scientific_name} = $mysql_input_ncbi_id;
      $db{$mysql_table}{$mysql_input_common_name} = $mysql_input_ncbi_id;

      ##  Create a string with the list of available options, which can be used as a message to the user
      my $concatenate_str = "";
      if ($mysql_input_ncbi_id == 0) {
        $concatenate_str = $mysql_input_ncbi_id.", ".$mysql_input_common_name;
      }
      else {
        $concatenate_str = $mysql_input_ncbi_id.", ".$mysql_input_scientific_name.", ".$mysql_input_common_name;
      }

      if (!defined ($db_options_str{$mysql_table})) {
        $db_options_str{$mysql_table} = $concatenate_str;
      }
      else {
        $db_options_str{$mysql_table} = $db_options_str{$mysql_table}.", ".$concatenate_str;
      }
    }
  }
}

##  Disconnect with the database
$db_handler -> disconnect();


########################################
##  Read in the input header and process it
########################################

##  Change the output mode to UTF-8 (See https://perldoc.perl.org/functions/binmode)
binmode STDOUT, ':encoding(UTF-8)';
binmode STDERR, ':encoding(UTF-8)';

my $csv_in = Text::CSV -> new ({ binary => 1, auto_diag => 1 });
open (my $input_fp, "<:encoding(UTF-8)", $input_arg) or die "EE\tCould not open $input_arg!\n";

##  Take the header and check it has at least the required number of columns

my $row = $csv_in -> getline ($input_fp);

##  Remove any "*" from the header, since they are there for the user; this script and the system will fail if they are still present
$row =~ s/\*//gs;

##  Store the column headers as they are processed
my @header_record;

for (my $k = 0; $k < $num_columns{$datatype_arg}; $k++) {
  if (!defined ($row -> [$k])) {
    $tmp_str = sprintf "Input table has an insufficient number of columns in header.  At least %u is expected for %s.\n", $num_columns{$datatype_arg}, $datatype_arg;
    $report_str = $report_str.$tmp_str;
    $program_return_value = $EXIT_FAILURE;
    last;
  }
  elsif (($row -> [$k]) =~ /^\s*$/) {
    $tmp_str = sprintf "The header of the input table should be the first row and it must remain unchanged.  If you have mistakenly deleted it, download a new version and copy the header row back in.\n";
    $report_str = $report_str.$tmp_str;
    $program_return_value = $EXIT_FAILURE;
    last;
  }
  else {
    if ($k == 0) {
      if ($row -> [$k] =~ /\x{FEFF}(.+)$/) {
        $tmp_str = "Unicode byte order marker found.\n";
        $report_str = $report_str.$tmp_str;

        ##  Remove the byte order marker from the file
        $row -> [$k] = $1;
      }
      else {
        $tmp_str = "Unicode byte order marker absent.\n";
        $report_str = $report_str.$tmp_str;
      }
    }

    push (@header_record, $row -> [$k]);
  }
}

##  If the header has a problem, then don't bother processing it
if ($program_return_value != $EXIT_FAILURE) {
  ##  Counter for number of records
  my $id = 0;

  ########################################
  ##  Read in each record
  ########################################

  ##  Read in each record
  while (my $row = $csv_in -> getline ($input_fp)) {
    my @tmp_record;
    for (my $k = 0; $k < $num_columns{$datatype_arg}; $k++) {
      if (defined ($row -> [$k])) {
        push (@tmp_record, $row -> [$k]);
      }
    }

    ##  Add the current row to the array of rows
    push @records, [ @tmp_record ];

    $id++;
  }

  $tmp_str = sprintf "Number of %s records read:  %u\n", $datatype_arg, $id;
  $report_str = $report_str.$tmp_str;

  close ($input_fp) or die "$input_arg: $!";


  ########################################
  ##  Check and clean up each record
  ########################################

  ##  For each record
  for my $k ( 0 .. $#records ) {
    my $aref = $records[$k];
    my $num_columns = @$aref;

    ##  Check that the number of columns is correct; if not,
    ##    print a message, then skip this record,
    ##    but keep going through the file
    if ($num_columns != $num_columns{$datatype_arg}) {
      $tmp_str = sprintf "Record %u is incomplete.  %u field(s) found but exactly %u was expected for %s.", $k + 1, $num_columns, $num_columns{$datatype_arg}, $datatype_arg;

      ##  Special case when there is a blank row
      if ($num_columns == 1) {
        $tmp_str = $tmp_str."  Any blank rows at the end of the table should be removed before uploading.\n";
      }
      else {
        $tmp_str = $tmp_str."\n";
      }

      $report_str = $report_str.$tmp_str;
      $program_return_value = $EXIT_FAILURE;
      next;
    }

    ##  For each cell of each record, check / process the value
    for (my $m = 0; $m < $num_columns; $m++) {
      my $old_value = $records[$k][$m];
      my ($return_value, $new_value, $message) = checkValue ($old_value, $datatypes{$datatype_arg}[$m]);

      ##  If there is a message to print, print it
      if (length ($message) != 0) {
        $tmp_str = sprintf "Record %u, value for \'%s\' %s\n", $k + 1, $header_record[$m], $message;
        $report_str = $report_str.$tmp_str;
      }

      ##  If the return value is not a success, store it as well
      if ($return_value != $EXIT_SUCCESS) {
        $program_return_value = $return_value;
      }

      ##  Update the cell's value
      $records[$k][$m] = $new_value;
    }
  }


  ########################################
  ##  Output the records out
  ########################################

  # https://metacpan.org/pod/Text::CSV
  my $csv_out = Text::CSV -> new ({ binary => 1, auto_diag => 1 });
  open (my $output_fp, ">:encoding(UTF-8)", $output_arg) or die "EE\tCould not open $output_arg!\n";

  ##  Output the header
  $csv_out -> say ($output_fp, \@header_record);

  ##  Output all of the records
  $csv_out -> say ($output_fp, $_) for @records;

  ##  Close the output file pointer
  close ($output_fp) or die "$output_arg: $!";
}


########################################
##  Output the report
########################################

open (my $report_fp, ">", $report_arg) or die "EE\tCould not open $report_arg!\n";
printf $report_fp "%s", $report_str;
close ($report_fp) or die "$report_arg: $!";


########################################
##  Exit from the script, returning the result to the shell
########################################

if ($config -> get ("debug")) {
  printf STDERR "II\tExit return value:  %u\n", $program_return_value;
}
exit ($program_return_value);

