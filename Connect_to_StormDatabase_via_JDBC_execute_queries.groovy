import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.sql.Connection;



String BPPMHostName = "somehost";
String BPPMDBPort = "2638";
String BPPMDBName = "storm_somehost";
String BPPMDBUser = "dba";
String BPPMDBPassword = "***";

DateFormat dateFormatforlog = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

def log = "";

if (BPPMDBPort.equals("")) {
    BPPMDBPort = "2638";
}

if (BPPMDBName.equals("")) {
    BPPMDBName = "storm_" + BPPMHostName.toLowerCase();
}

if (BPPMDBUser.equals("")) {
    BPPMDBUser = "dba";
}



ArrayList < String > serversforPCMMEM = new ArrayList < String > ();
ArrayList < String > serversinBPPM = new ArrayList < String > ();
ArrayList < String > serversinBPPMUnique = new ArrayList < String > ();
ArrayList < ArrayList < String >> allserverinfo = new ArrayList < ArrayList < String >> ();


Connection dbconn = null;

Class.forName("sap.jdbc4.sqlanywhere.IDriver");



DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
java.util.Date date = new java.util.Date();
String curdate = dateFormat.format(date);



ResultSet bppmstviewtimeseries = dbconn.createStatement().executeQuery("select instanceinfo_cfg.devicename, min(DATEFORMAT(dateadd(SS,CONVERT(INT, _PATROL__CPU_ST_VIEW.TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime)), 'yyyy-mm-dd hh:nn:ss')) as stviewmin,max(DATEFORMAT(dateadd(SS,CONVERT(INT, _PATROL__CPU_ST_VIEW.TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime)), 'yyyy-mm-dd hh:nn:ss')) as stviewmax from instanceinfo_cfg inner join _PATROL__CPU_ST_VIEW on instanceinfo_cfg.MOINSTID = _PATROL__CPU_ST_VIEW.MOINSTID group by instanceinfo_cfg.devicename");

while (bppmstviewtimeseries.next()) {
    bppmstviewtime.add(bppmstviewtimeseries.getString(1).split("\\.")[0].trim().toLowerCase() + "," + bppmstviewtimeseries.getString(2) + "," + bppmstviewtimeseries.getString(3));
    serversforPCMMEM.add(bppmstviewtimeseries.getString(1));
}
//println("bppmstviewtime" + bppmstviewtime)
bppmstviewtimeseries.close();

try {
    for (int i = 0; i < serversforPCMMEM.size(); i++) {
        //println "for each server in pcmmem";
        String ossname = serversforPCMMEM.get(i);

        StringBuilder sb = new StringBuilder();
        String bppmitimecpuseriesquery = "";
        String bppmitimememseriesquery = "";
        //   String bppmitimecpuseriesquery2 = "";
        // String bppmitimememseriesquery2 = "";

        bppmitimecpuseriesquery = "select DATEFORMAT(dateadd(SS,CONVERT(INT, TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-mm-dd hh:nn:ss') as y,CPUCPUUTIL from _PATROL__CPU_ST_VIEW where CPUCPUUTIL IS NOT NULL and MOINSTID in (select MOINSTID from instanceinfo_cfg where devicename = '" + serversforPCMMEM.get(i) + "'  and instname = 'CPU') order by y";

        bppmitimememseriesquery = "select DATEFORMAT(dateadd(SS,CONVERT(INT, TIMERECORDED), CAST('1970-01-01 00:00:00' as datetime) ), 'yyyy-mm-dd hh:nn:ss') as y,MEMUsedMemPerc from _PATROL__MEMORY_ST_VIEW where MEMUsedMemPerc IS NOT NULL and MOINSTID in (select MOINSTID from instanceinfo_cfg where devicename  = '" + serversforPCMMEM.get(i) + "' and instname = 'MEMORY') order by y";


        String osres = "";
        String ostechser = "";

        ResultSet rscpu = dbconn.createStatement().executeQuery(bppmitimecpuseriesquery);
        ResultSet rsmem = dbconn.createStatement().executeQuery(bppmitimememseriesquery);
        while (rscpu.next() && rsmem.next()) {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            java.util.Date targetdate = originalFormat.parse(rscpu.getString(1));
            String cpuutil = "";
            String memutil = "";
            String diskutil = "";
            if (!rscpu.getString(2).equals("") && rscpu.getString(2) != null && !rscpu.getString(2).isEmpty()) {
                if (rscpu.getString(2).contains(".")) {
                    if (rscpu.getString(2).split("\\.")[1].size() > 2) {
                        cpuutil = rscpu.getString(2).split("\\.")[0] + "." + rscpu.getString(2).split("\\.")[1].substring(0, 2);
                    } else {
                        cpuutil = rscpu.getString(2);
                    }
                } else {
                    cpuutil = rscpu.getString(2);
                }
            } else {
                cpuutil = "";
            }
            if (!rsmem.getString(2).equals("") && rsmem.getString(2) != null && !rsmem.getString(2).isEmpty()) {
                if (rsmem.getString(2).contains(".")) {
                    if (rsmem.getString(2).split("\\.")[1].size() > 2) {
                        memutil = rsmem.getString(2).split("\\.")[0] + "." + rsmem.getString(2).split("\\.")[1].substring(0, 2);
                    } else {
                        memutil = rsmem.getString(2);
                    }
                } else {
                    memutil = rsmem.getString(2);
                }
            } else {
                memutil = "";
            }

            sb.append(targetFormat.format(targetdate) + "," + nodenameinoss + ",," + cpuutil + "," + memutil + ",,,,,," + osres + "," + ostechser + ",,,,,,,\n");
        }
        rscpu.close();
        rsmem.close();

        ResultSet rscpu2 = dbconn.createStatement().executeQuery(bppmitimecpuseriesquery2);
        ResultSet rsmem2 = dbconn.createStatement().executeQuery(bppmitimememseriesquery2);
        while (rscpu2.next() && rsmem2.next()) {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            java.util.Date targetdate = originalFormat.parse(rscpu2.getString(1));
            String cpuutil = "";
            String memutil = "";
            String diskutil = "";
            if (!rscpu2.getString(3).equals("") && rscpu2.getString(3) != null && !rscpu2.getString(3).isEmpty()) {
                if (rscpu2.getString(3).contains(".")) {
                    if (rscpu2.getString(3).split("\\.")[1].size() > 2) {
                        cpuutil = rscpu2.getString(3).split("\\.")[0] + "." + rscpu2.getString(3).split("\\.")[1].substring(0, 2);
                    } else {
                        cpuutil = rscpu2.getString(3);
                    }
                } else {
                    cpuutil = rscpu2.getString(3);
                }
            } else {
                cpuutil = "";
            }
            if (!rsmem2.getString(3).equals("") && rsmem2.getString(3) != null && !rsmem2.getString(3).isEmpty()) {
                if (rsmem2.getString(3).contains(".")) {
                    if (rsmem2.getString(3).split("\\.")[1].size() > 2) {
                        memutil = rsmem2.getString(3).split("\\.")[0] + "." + rsmem2.getString(3).split("\\.")[1].substring(0, 2);
                    } else {
                        memutil = rsmem2.getString(3);
                    }
                } else {
                    memutil = rsmem2.getString(3);
                }
            } else {
                memutil = "";
            }

            sb.append(targetFormat.format(targetdate) + "," + ossname + "," + cpuutil + "," + memutil + "\n");
        }
        rscpu2.close()

        println("CPU, memory utilization below.")
        println(sb.toString()) // this can be pushed into a file using PrintWriter
    }
} catch (IOException e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String exceptionAsString = sw.toString();
    return [1, "Error Occured. Exception: " + exceptionAsString + "\n" + log]
}

dbconn.close()

return [0, log]