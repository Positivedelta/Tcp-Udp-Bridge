//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.applications;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.jar.JarFile;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.config.BridgeConfig;
import bitparallel.communications.ConnectionServer;

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
        var buildDate = "Not Available";
        var buildHash = "Not Available";
        try
        {
            final var jarFile = new File(TcpUdpBridge.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarFile.isFile())
            {
                final var jar = new JarFile(jarFile);
                final var manifest = jar.getManifest();
                final var attributes = manifest.getMainAttributes();
                buildDate = attributes.getValue("Build-Date");
                buildHash = attributes.getValue("Build-Git-Hash");
            }
        }
        catch (final Exception ex)
        {
            logger.error("Unable to extract the build information from the associated JAR file");
            logger.error(ex.getMessage());
        }

        logger.info("TCP to UDP Bridge, developed by Bit Parallel Ltd in May 2025");
        logger.info("Build date: " + buildDate);
        logger.info("Git commit hash: " + buildHash);

        final var config = new BridgeConfig();
        final var server = new ConnectionServer(config);
        server.start();
    }
}
