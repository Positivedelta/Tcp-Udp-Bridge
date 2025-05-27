//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.applications;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.config.BridgeConfig;

public class TcpUdpBridge
{
    static
    {
        try
        {
            // setup log4j v2, configured to create a distinct log each time the application is run
            //
            final var logFormatter = new SimpleDateFormat("yyyyMMdd-HHmmss-");
            logFormatter.setTimeZone(TimeZone.getTimeZone("Zulu"));

            final var applicationLog = new StringBuilder();
            applicationLog.append("logs");
            applicationLog.append(File.separator);
            applicationLog.append(logFormatter.format(new Date()));
            applicationLog.append("tcp-udp-bridge.txt");

            // referenced in the log4j.xml
            // note, log4j cannot be used until the following environment variable has been set
            //
            System.setProperty("tcpUdpBridgeApplicationLogFilename", applicationLog.toString());
        }
        catch (final Exception ex)
        {
            System.out.println("Fatal Error! Unable to load the Log4J application log file setup, reason: " + ex.getMessage());
            System.exit(0);
        }
    }

    private static final Logger logger = LogManager.getLogger(TcpUdpBridge.class);

    public final static void main(final String[] args)
    {
//      logger.info("TCP to UDP Bridge"); // FIXME! add the git build information

        final var p1 = new BridgeConfig();
        for (final var endpoint : p1.getConnections()) System.out.println(endpoint);
        System.out.println();
        System.out.println();

        for (final var endpoint : p1.clone().getConnections()) System.out.println(endpoint);
    }
}
