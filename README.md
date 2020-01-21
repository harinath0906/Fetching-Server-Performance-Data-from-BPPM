# How to Fetch Server Performance Data from BPPM

BMC ProactiveNet Performance Management is an integrated platform that combines event management and data analytics in a single seamless solution. It goes beyond monitoring to handle complex IT environments and diverse data streams to deliver actionable IT intelligence. 

BMC stores its data in a storm database which can be accessed using SQL Anywhere which can be downloaded from below.

https://archive.sap.com/documents/docs/DOC-35857

## Information about tables holding the data

Table: instanceinfo_cfg

Column: devicename

Description: Name of the device under monitoring in BPPM


Table: _PATROL__NT_HEALTH_ST_VIEW

Column: MemoryUsage

Description: Percentage of time busy servicing requests for this device


Table: _PATROL__NT_HEALTH_ST_VIEW

Column: ProcessorUtilization

Description: The % of time that this CPU was not idle during the interval


Table: _PATROL__NT_HEALTH_ST_VIEW

Column: DiskUsage

Description: The % of time that this disk was servicing requests for this device


Table: _PATROL__NT_HEALTH_ST_VIEW

Column: TIMERECORDED

Description: This is the time (recorded every 2 minutes)


Table: _PATROL__NT_HEALTH_RT_VIEW

Column: MEMORYUSAGE_AVG

Description: Percentage of time busy servicing requests for this device


Table: _PATROL__NT_HEALTH_RT_VIEW

Column: PROCESSORUTILIZATION_AVG

Description: The % of time that this CPU was not idle during the interval


Table: _PATROL__NT_HEALTH_ST_VIEW

Column: DISKUSAGE_AVG

Description: The % of time that this disk was servicing requests for this device


Table: _PATROL__NT_HEALTH_RT_VIEW

Column: TIMERECORDED

Description: This is the time (recorded every 1 hour)


Table: _PATROL__FILESYSTEM_ST_VIEW

Column: FSCAPACITY, FSAVAILABLESPACEMB,FSUSEDSPACEMB

Description: Total Space, Available Space, Used Space


Table: _PATROL__NT_LOGICAL_DISKS_ST_VIEW

Column: LDldFreeSpacePercent, LDldFreeMegabytes,LDldDiskSpaceUsed

Description: Free Space %, Available Space, Used Space

Hence we came up with the below queries

### Query to fetch name of devices under monitoring 
select distinct devicename from instanceinfo_cfg

### Queries to fetch CPU, Memory and Disk utilization of the device
select DATEFORMAT(dateadd(SS,CONVERT(INT, TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-mm-dd hh:nn:ss') as y, MemoryUsage,ProcessorUtilization,DiskUsage from _PATROL__NT_HEALTH_ST_VIEW where MOINSTID in (select MOINSTID from instanceinfo_cfg where instname = 'Health At A Glance' and devicename = 'somehost') 

select DATEFORMAT(dateadd(SS,CONVERT(INT, FROMTIME), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-MM-dd Hh:nn:ss') as x,DATEFORMAT(dateadd(SS,CONVERT(INT, TOTIME), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-MM-dd Hh:nn:ss') as y,MEMORYUSAGE_AVG,PROCESSORUTILIZATION_AVG,DISKUSAGE_AVG from _PATROL__NT_HEALTH_RT_VIEW where MOINSTID in (select MOINSTID from instanceinfo_cfg where instname = 'Health At A Glance' and devicename = 'somehost')

In case we want to also apply time filter
select select DATEFORMAT(dateadd(SS,CONVERT(INT, TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-mm-dd hh:nn:ss') as y, MemoryUsage,ProcessorUtilization,DiskUsage from _PATROL__NT_HEALTH_ST_VIEW where MOINSTID in (select MOINSTID from instanceinfo_cfg where instname = 'Health At A Glance' and devicename =  'somehost') and (y < '2017-07-25 07:50:00'  or y  > '2017-10-18 04:55:00') order by y

select DATEFORMAT(dateadd(SS,CONVERT(INT, FROMTIME), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-MM-dd Hh:nn:ss') as x,DATEFORMAT(dateadd(SS,CONVERT(INT, TOTIME), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-MM-dd Hh:nn:ss') as y,MEMORYUSAGE_AVG,PROCESSORUTILIZATION_AVG,DISKUSAGE_AVG from _PATROL__NT_HEALTH_RT_VIEW where MOINSTID in (select MOINSTID from instanceinfo_cfg where instname = 'Health At A Glance' and devicename = 'somehost') and (x < '2017-07-25 07:50:00'  or x  > '2017-10-18 04:55:00') order by x

### Queries to list all filesystems of a given hostname

select INSTNAME from instanceinfo_cfg where devicename like 'GBBLI015.gb-cdc01.nxp.com' and (CONFIGVALUES like '%FileSystemType=NFS%' or CONFIGVALUES like '%FileSystemType=EXT%')

### Queries to list hostname, time, UsedSPACEMB,AVAILABLEPSACEMB,% used of a specific filesystem and device

select DATEFORMAT(dateadd(SS,CONVERT(INT, TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-mm-dd hh:nn:ss') as y,FSCAPACITY, FSAVAILABLESPACEMB,FSUSEDSPACEMB from _PATROL__FILESYSTEM_ST_VIEW where MOINSTID in (select MOINSTID from instanceinfo_cfg where devicename like 'somehost'  and instname = '/central/auditlogs')  order by y

### Queries to list all disks of a given hostname

select INSTNAME from instanceinfo_cfg where devicename like 'somehost'  and INSTNAME like '%:)'

### Queries to list UsedSpacePercent and TotalSpaceMB for a device and Disk

select DATEFORMAT(dateadd(SS,CONVERT(INT, TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-mm-dd hh:nn:ss') as y,(100-LDldFreeSpacePercent) as UsedSpacePercent, (LDldFreeMegabytes+LDldDiskSpaceUsed) as TotalSpaceMB from _PATROL__NT_LOGICAL_DISKS_ST_VIEW where MOINSTID in (select MOINSTID from instanceinfo_cfg where devicename like 'somehost'  and instname = 'Application 1 (F:)')  order by y
