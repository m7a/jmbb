---
x-masysma-name: jmbb
section: 32
title: Java Ma_Sys.ma Block Backup JMBB
keywords: ["jmbb", "backup", "readme"]
lang: en-US
x-masysma-version: 1.0.3
date: 2014/08/06 01:16:35
x-masysma-copyright: |
  Copyright (c) 2013, 2014, 2015, 2017, 2019, 2020 Ma_Sys.ma.
  For further info send an e-mail to Ma_Sys.ma@web.de.
  This program's encryption functions are modifications of
  Java AESCrypt, Copyright 2008 Vócali Sistemas Inteligentes.
  For further information refer to http://www.aescrypt.com/java_aes_crypt.html.
x-masysma-website: https://masysma.lima-city.de/32/jmbb.xhtml
x-masysma-repository: https://www.github.com/m7a/lo-jmbb
x-masysma-owned: 1
---
Description
===========

JMBB allows you to create encrypted incremental backups from source directories
to a destination directory. Unlike most encrypting backups JMBB does not use
encryption on per-file basis. Instead, it adds multiple files to a compressed
and encrypted archive file called “block”. This securely encryptes the source
directory structure and all file metadata. Whenever files have changed, they
are added to new blocks at the next incremental backup. When all files
contained in a block have been replaced by files from newer blocks (i.e. when
all files in an old block were changed over time) the old block is deleted.
This ensures that with common usecases the incremental backup will not be
bigger than the source data.

Basic Usage
===========

An automatically generated usage information which also conatins “long”
arguments can be obtained by invoking JMBB with `--help`.

JMBB is usually run through either a `jmbb` command if it was installed on UNIX
or Linux or with `java -jar JARFILE` with JARFILE being the `.jar` file
containing the program.

Example: Creating a backup with JMBB from `/data` to `/media/backup`

	$ java -Xmx5G -jar jmbb.jar -o /media/backup -i /data

If you are getting an out of memory error add suitable `-XmxNG` values with N
being enough RAM for the program to work. On Linux you can enter about as much
as your amount of physical RAM + size of available swap space. 

JCE Dependency
==============

On Windows and all other systems where they are not installed by default you
need the “Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction
Policy Files” from
<http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html>
or JMBB will not encrypt or decrypt and therefore not work at all. This is
related to the fact, that JMBB uses AESCrypt's strong cryptography which you
need to enable separately for your Java installation. To resolve this, JMBB
would either need to package the file with it (licensing issues prevent me from
doing this) or implement the whole AES stuff itself which would lead to more
bugs in the code and is therefore also not an option. 

Intention
=========

JMBB was created with the following scenario in mind: The user wants to create
an online “cloud” backup but wants to encrypt his/her files. However,
transferring a whole, encrypted 7z archive for every backup is very
time-consuming. Instead, the user wants to create an incremental backup. The
best solutions widely known are either “encfs” or “Box Cryptor” both of which
act on per file basis and therefore not encrypt access times, file sizes and
only obfuscate filenames, not encrypt directory structures. JMBB can not
directly synchronize with an online storage but write to any directory which
may then be synced using the client program provided by the “cloud service”.
JMBB is an incremental backup program, that means neither a program to
synchronize files, nor a program to create disk images or backup system
installations. The technical limitations (cf. tables below) make JMBB unuseable
for handling large files of 8 GiB or larger. Creating a backup of 8 GiB or
more data however, is not an issue. 

Maintenance
===========

JMBB has an internal editor for database maintenace tasks. It can be started
as follows:

	$ jmbb -e BACKUP

The editor can be used interactively to set new passwords, display existing
passwords, display database statistics and clear blocks which mainly consist of
obsolete files. Also, it allows the user to view contents of a specific block
as they are registered in the database. If a password has been cracked or
leaked, the editor can also “deprecate” passwords, i. e. force all blocks
using the obsolete password to be re-created on the next JMBB invocation.

Transition to 1.0.1.0
=====================

Before 1.0.1.0, sometimes an incremental backup would create sparsely filled
blocks without an apparent reason. This was a result of how JMBB decided which
files were to be added to blocks – files which matched their previous version
were not added at the stage of block creation when it as already defined which
files would be contained in a block. Using programs which rewrite a lot of data
which has not chagned upon program exit, this caused empty blocks. Although
empty blocks were no “danger”, they had a major issue: JMBB did not ever delete
them because their contained files never changed to obsolete (because there
were no files contained).

With Version 1.0.1.0, this issue has been fixed by applying the following:
Checksums are now calculated at the stage of block file list creation in a
single threaded manner if the checksum could influence if the file is added to
a block or not. Therefore, empty blocks will no longer be created. To be able
to clean your old backup tree, a new command `empty` has been added to list and
with `emtpy rm` delete all empty blocks. It has been tested that empty blocks
are not relevant for restoration.

Transition to 1.0.2.0
=====================

Before 1.0.2.0, JMBB did not store any information whenever a file was found to
have a newer timestamp but already existed in the database with the same
checksum. The main idea about this was to avoid backing up a known file again
and instead just keep the existing (copy) where the modification time is older
but all other metadata (and the content) are equal.

Upon upgrading to Debian Stretch with Java 8, a strong disadvantage of this
approach was found: Due to the upgrade, the modification times of existing
files could be read more exactly, i. e. instead of always being 000, the last
three digits of the modification time in milliseconds suddenly got a value for
a lot of files. By this, JMBB found a lot of files with different timestamps
but only few of them had changed, making it update only the copies of said few
files. The other (unchanged) files' new times were not stored leading to very
long scanning times due to checksuming _all_ files in the filesystem where new
times were available upon _each invocation_.

While one could have started the backups from scratch or reduce the accuracy of
metdata, another approach was chosen (cf. `DBNewTimes.java` in the source code
for reasoning about the design decision): A new place in the “database” XML
file is now used to track files which have only changed in modification time
and thus not been backed up again. For these files, whenever the file is
scanned again with the same time, it is treated as already present in the
backup (which it is except for an older timestamp) making it unnecessary to
re-checksum the affected files upon every invocation.

Compatibility with previous versions
:   Currently, the additional database entries are not used for restoration
    which means the new JMBB can restore (and update) backups created with
    previous versions. Upon updating an old backup, JMBB will automatically
    create the new structure in the XML file.

Short Summary
:   If you experience unusually long backup update times after a Java and/or OS
    upgrade (with a lot of HDD activity), this update is likely to solve the
    issue after the 2nd (!) backup update done with it.

With this version update, no manual database editing is necessary.

Updates in 1.0.3
================

No changes should be necessary. This release fixes an issue with backup
transactionality: In case a backup were updated and some files that existed
already previously were changed but no longer readable (e. g. due to
intermittent deletion or `chmod 000`), then this could cause a backup
inconsistency (blocks being deleted but not marked in the database as such).
This version attempts to fix the issue by only deleting blocks after the DB
with the changes could be saved successfully.

Updates in 1.0.4 to 1.0.6
=========================

No changes should be necessary. This release adds the integrity check feature
described in section _Performing Integrity Checks_.

Tables
======

## JMBB Features and their respective implementations

Feature         Implementation
--------------  --------------------------------------------------------
Archiving       GNU CPIO "Portable ASCII Format" (filesize limit: 8 GiB)
Compression     XZ For Java <http://tukaani.org/xz/java.html>
Encryption      AESCrypt AES256, see copyright <http://aescrypt.com/>
Database        GZipped XML with DTD included in JMBB JAR
File traversal  Java NIO for backup source directory traversal

## Envorinoment variables affecting backup creation. Use with care

Variable        Description
--------------  --------------------------------------------------------
`JMBB_THREADS`  Number of XZ threads to create. Default: number of cores
`JMBB_XZLEVEL`  Changes the XZ compression level from 1 to 9, default: 8
`JMBB_WINDOWS`  `true` means warnings about failed `stat()`s are hidden

## JMBB performance comparison table

Command                            User/s  Sys/s  Real/s  MaxRes/k  Comp/O
---------------------------------  ------  -----  ------  --------  ------
`jmbb -o BAK -i SRC`               1231    71     298     3959444   0.3122
`tar -c SRC | 7z* BAK`             822     7      239     3785532   0.3065
`jmbb -r DST -s BAK`               162     15     175     478300    n/a
`7z x -so BAK p* | tar -C DST -x`  38      4      53      68300     n/a

### Command aliases for the JMBB performance comparison table 

 * `p* := -ptestwort`
 * `7z* := 7z a -t7z -m0=lzma2 -mx=9 -mfb=64 -md=64m -ms=2g -l p* -si -bd`

Machine and OS Requirements and Recommendations
===============================================

## System requirements 

 * Linux or UNIX with GNU CPIO
 * Java 7 or higher Runtime Environment (only Java 7 and 8 known to work). For
   more than three (virtual) cores a 64 Bit JVM is required.
 * Enough HDD space to store the newly created backup.
 * RAM requirements (see below)

## System recommendations 

 * Multicore (4+ recommended) system with much RAM (6 GiB+ recommended).

JMBB is a “heavyweight” Java program which requires a lot of RAM depending on
your system because it creates distinct XZ compressors for every processor core
and because it reads it's whole database into your RAM.

Approximately you will need

	300 MiB + (virtual) cores * 600 MiB free RAM.
	=> old singlecore: 1 GiB of free RAM recommended.
	=> new quadcore:   5 GiB of free RAM recommended (rem: 8 virtual cores).

You might need more than 300 MiB of additional RAM depending on the size of the
directory tree you want to backup. Also, the amount of RAM required for the
data base slowly grows with each incremental backup. This is currently an
unfortunate design error/requirement.

If you do not fulfil the RAM requirements but still want to use JMBB you might
be able to tune the amount of memory required by lowering compression settings
or reducing the number of threads to be created. These values can be affected
by environment variables as listed in the table above.

Also, the increasing RAM usage of the database is considered a “known issue”
which should be resolved in the future. Be aware, however, that a solution to
that issue will at least require you to convert your database if not to switch
to anoter program.

Performing Integrity Checks
===========================

Since version 1.0.6, a new mode of invoking JMBB has been added: The integrity
check. The idea behind the integrity check is to supply a database file and
a directory that contains `.cxe` files. JMBB will then attempt to decrypt all
of the blocks and compare their checksum against the value stored in the
database. Additionally, JMBB checks if all blocks necessary to restore the
backup state from the database are present on disk.

Despite being a computationally and I/O intensive operation, the integrity
check can achieve good performance: Below invocation ran on about 250 GiB of
block data and finished in about half an hour, i.e. averaged 140 MiB/s on an
Intel Core i7-4770 with 24 GiB of RAM and a software RAID 1 of SATA HDDs.

	$ jmbb -I /fs/backuphist/metadata/db.xml.gz -R /fs/backuphist/blocks
	Details
	=======

	0000000000000001  [ ok ]  obsolete, absent
	0000000000000002  [ ok ]  obsolete, absent
	0000000000000003  [ ok ]  obsolete, absent
	[...]
	00000000000013b2  [FAIL]  active,   absent
	00000000000013b3  [FAIL]  active,   absent
	00000000000013b4  [FAIL]  active,   absent
	[...]
	00000000000033a8  [ ok ]  active,   verified
	00000000000033a9  [ ok ]  active,   verified
	
	Statistics
	==========

	[ ok ]  active,   verified               3098
	[ ok ]  obsolete, verified               8942
	[ ok ]  obsolete, absent                 915
		-- SUM                           12955
	[FAIL]  active,   absent                 269

	Summary
	=======

	BACKUP IS INCONSISTENT!

The report is structured as follows:

## Details

All blocks are listed in a sorted and tabular fashion. The table columns are as
follows:

 1. Block ID: The number of the block
 2. `[ ok ]` or `[FAIL]` depending on whether this block is good or not.
    A block is good if either (a) it exists on disk and its checksum matches
    OR (b) it does not exist on disk and is no longer needed (obsolete).
 3. Whether the block under consideration is needed (`active`) or obsolete.
 4. Whether the block is present on disk and verified (`verified`),
    not present on disk (`absent`) or present but not the same as recorded
    in the database (`CHECKSUM MISMATCH`). In case multiple block files exist
    with the same name, their individual file paths and whether they match the
    database will also be reported in separate lines following the current
    entry. In case you are interested in the complete set of messages that
    can appear here, check file `ma/jmbb/IRStatus.java`.

## Statistics

This section counts each of the result messages' occurrences. In case of
a backup archive there might be a constant number of blocks in the
`obsolete absent` category that should not increase (unless the archive
is losing blocks!). Hence, these statistics allow insights into the completeness
of such an archive beyond a simple “is able to restore”.

## Summary

This section will only ever display one of two messages:

`BACKUP IS INCONSISTENT!`
:   At least one of the active blocks is absent or
    at least one block file was found to have a mismatching checksum.
`Backup is consistent.`
:   All blocks on disk match the database's contents and it should be possible
    to restore the state of the database from the blocks. Note: JMBB does not
    extract all of the blocks' contents, thus a minor uncertainity remains that
    the blocks' contents may have been invalid in the first place. Such
    anomalies can currently only be detected by performing a proper restore and
    then comparing against the original data.

Compiling
=========

As JMBB is a Java-Application it is normally unnecessary to recompile it. But
if you extract the source from the jarfile, you can compile JMBB with `$ ant`
to generate all .class files or `$ ant jar` to generate the jmbb.jar

Redistribution
==============

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see <http://www.gnu.org/licenses/>.

Note
:   You can view the GPL by either browsing the JMBB jarfile or running JMBB
    with the `-$` parameter. The full sourcecode is also part of the jarfile.

Emergency
=========

JMBB was designed to allow the user to restore his/her data even if many things
are lost. Depending on the situation, most of the restoration can be done by
JMBB itself. Should JMBB not be able to run or should a compatible `cpio`
implementation not be available, it is possible to restore the data from the
blocks. To restore all data (including possibly deleted files) from a block, it
can be extracted with common utilities all of which may be substituted if
necessary.

Block files `.cxe` are CPIO XZ Encrypted files which means that they can be
extracted in reverse order of creation, e. g.:

	$ aescrypt -d -p PASSWORD -o result.cpio.xz block.cxe
	$ xzcat result.cpio.xz | cpio -i

Apart from user data, every block file contains a file `meta.xml` with
checksums and information about the contained files. The “global” database file
applying to the whole backup contains everything including all `meta.xml`
files. It can be used to find out about which files should be restored and
which files were deleted or replaced by another file. Usually, this is done by
the JMBB restoration function … it is listed here for emergency cases only.

To reduce the possiblity of such an emergency where JMBB might not be
available, it is advisable to store JMBB and a “reference” CPIO implementation
next to the backup, ideally in the same folder the backup resides in.

Alternatives
============

Relying on backup software used by few people, which JMBB is for example, is
always a security risk: No experts have reviewed the sourcecode and chances are
bad data can be recovered in case of failure. To mitigate the potential
negative aspects, JMBB relies on _standard formats_ for all of its features.
Still, some risks remain. To give an overview about the alternatives which are
used by more users, there used to be a list of alternatives here.

Since 2021/04/10, a new and more exhaustive analysis of the alternatives
exists under [backup_tests_borg_bupstash_kopia(37)](../37/backup_tests_borg_bupstash_kopia.xhtml).

Advanced Usage
==============

Creating and updating a backup with JMBB is simple enough. However, JMBB was
also designed to be used for complex backup strategies. As an example, this
section describes how to setup a backup strategy similar to JMBB's author's.

## Integration into a Backup Strategy

My backup consists of multiple layers: A quick incremental backup to another
internal HDD is created on every shutdown and an encrypted copy is sent to a
separate PC for synchronizing the data to a cloud service. Its main purpose is
to record changes and allowing to fetch older versions of files if a file was
accidentally deleted or changed. About once per week, a backup of important data
and programs is copied to an external SSD. This backup strategy is implemented
using JMBB and standard Linux utilities.

### The backup upon shutdown

 * `$ jmbb -o $HOME/backup -i /data/main | copydelta.sh`
 * `copydelta.sh` collects new blocks into a separate directory.

### The weekly backup to an external SSD

 * `$ jmbb -d $HOME/backup -c /mnt/backup`
 * Mirroring is sufficient.

### The system and data backup

 * `$ jmbb -d $HOME/backup -c /mnt/backup_system`
 * `$ rsync -av /data/programs /mnt/backup_system`

### The online backup.

 * `$ jmbb -d $HOME/backup -c $HOME/backup`
   This shows a special JMBB feature: A mirror can be stored in a database.
   By storing all blocks in a directory called "cnt" and an encrypted copy of
   the database with it, it is easily possible to sync "cnt" to a (possibly
   public) online storage without disclosing any information about your files.
   To extract the files, a copy of the DB (`cnt/../db.xml.gz`) or the password
   are required.
 * `$ onlinesync.sh $HOME/backup/cnt`
   `onlinesync.sh` is a hypothethical name of a script to synchronize a normal
   directory with the online storage.

### The archive backup

On an irregular schedule, new blocks that were collected upon shutdown are
archived to separate machine. After arriving there, the integrity check
feature is used to check the existent and newly created blocks' consistency.
This archive acts in a pull-based fashion and does not ever overwrite existent
data to achive some resistance against file corruption caused by malware.

## Wrapper scripts

As you can see, JMBB is best used in conjunction with other scripts and
utilities. Also, it is recommended not to invoke JMBB directly but create a
script to create an interface for your personal backup strategy in order to
make backups more convenient.

Wrapper scripts are also helpful to add additional utility invocations, provide
required environment variables (cf. table above) and to enter source
directories automatically.

Bug reporting
=============

Immediately report bugs informally with important information (OS,
architecture, CPIO, Java versions, etc.) and exact error message including
stack trace (if available) to the Ma_Sys.ma e-mail address listed at the very
beginning of this file. If you are able to reproduce the bug, add the minimum
amount of steps which result in the bug to your mail.

Known issues
============

You can currently not create restorable backups on Windows systems. This is a
bug which results from the treatment of file names: The fixed separator

`/` is assumed for CPIO's patterns but obviously CPIO's Windows version also
expects (and writes to the CPIO archives) real Windows filenames. Backups with
Unix filenames however, are restored correctly even on Windows systems if you
have a Windows version of CPIO.

### Bugs 

 * OutOfMemoryError does not cause nonzero exit status (but also does not update
   DB and therefore leaves it in a consistent state) → TEST
 * File Name Handling defect: File names which are invalid UTF-8 are not
   processed properly.
 * Studying AES-CBC in detail it seems that it might be good to make IV
   generation not only depend on the key but proably on the block number as
   well!

### Plans for enhanced robustness 

 * For a JMBB SQL variant breaking the program at arbitrary points should be
   fully supported by creating blocks transactionally (once block on HDD: commit
   metadata, before block to HDD: commit as “failed” which will be deleted upon
   reentering the program with the same block ID not being used again and the
   metadata not being discarded but marked as “failed”
 * It would be nice if JMBB handled FS changes sensibly: Upon doing a STAT for
   the file, the size should be noted. If it later changes → add to a
   notification list (incl. delta) and if it later disappears → repeat STAT and
   if it does not appear by then add it to a notification list. At the end of a
   JMBB invocation, a summary of size-changed and vanished files should be
   printed. (Does it make sense to do a 2-nd stat phase to detect _added_ files,
   too?)

### Misc. notes

 * `afio` may replace `cpio` with higher limits.
