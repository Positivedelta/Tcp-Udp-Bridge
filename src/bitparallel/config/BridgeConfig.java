//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class BridgeConfig extends Properties implements Endpoints
{
    private final static String JAR_RELATIVE_CONFIG_PATH = "config/";
    private final static String CONFIG_FILE_NAME = "config.properties";
    private final static File CONFIG_FILE = new File(JAR_RELATIVE_CONFIG_PATH + CONFIG_FILE_NAME);

    private final static String SERVER_PORT_NUMBER_KEY = "server.tcpport";
    private final static String SERVER_USE_ASSIGNED_IP_KEY = "server.useAssignedIp";
    private final static String SERVER_USE_SPECIFIED_IP_KEY = "server.useSpecifiedIp";
    private final static String ENDPOINT_PREFIX_KEY = "endpoint.";

    private final static String GOOGLE_PUBLIC_DNS = "8.8.8.8";
    private final static int GOOGLE_PUBLIC_DNS_DATAGRAM_CONNECT_PORT = 10002;

    private final static Logger logger = LogManager.getLogger(BridgeConfig.class);

    public BridgeConfig()
    {
        load();

        // FIXME! remove once tested...
        //
        clear();
        setProperty("server.useAssignedIp", "true");
        setProperty("server.useSpecifiedIp", "192.168.8.216");
        setProperty("server.tcpport", "11013");
        setProperty("endpoint.1", "192.168.8.249:1234");
        setProperty("endpoint.2", "192.168.8.250:1234");
        save();
    }

    public final void load()
    {
        if (CONFIG_FILE.exists())
        {
            try (final var is = new FileInputStream(CONFIG_FILE))
            {
                load(is);
            }
            catch (final IOException ex)
            {
                logger.error("Unable to read the config file: " + CONFIG_FILE);
                logger.error(ex.getMessage());
            }
        }
        else
        {
            logger.info("Missing config file, please create a valid configuration");
        }
    }

    public final void save()
    {
        try (final var os = new FileOutputStream(CONFIG_FILE))
        {
            storeToXML(os, "TCP to UDP Bridge config, last updated on: " + new Date());
        }
        catch (final IOException ex)
        {
            logger.error("Unable to save the config file: " + CONFIG_FILE);
            logger.error(ex.getMessage());
        }
    }

    public final SocketAddress getServer() throws ServerConfigurationException
    {
        if (containsKey(SERVER_PORT_NUMBER_KEY))
        {
            final var rawPort = getProperty(SERVER_PORT_NUMBER_KEY);
            try
            {
                final var port = Integer.parseInt(rawPort);

                if (containsKey(SERVER_USE_ASSIGNED_IP_KEY))
                {
                    if (Boolean.parseBoolean(getProperty(SERVER_USE_ASSIGNED_IP_KEY)))
                    {
                        try
                        {
                            // trick to figure out the "main outgoing" IP address, should handle multiple network interfaces
                            // FIXME! needs testing on Mac OS X, may return 0.0.0.0
                            //
                            final var socket = new DatagramSocket();
                            socket.connect(InetAddress.getByName(GOOGLE_PUBLIC_DNS), GOOGLE_PUBLIC_DNS_DATAGRAM_CONNECT_PORT);

                            final var ipAddress = socket.getLocalAddress();
                            socket.close();
                            logger.info("Detected locally assigned IP address: " + ipAddress);

                            return new InetSocketAddress(ipAddress, port);
                        }
                        catch (final Exception ex)
                        {
                            throw new ServerConfigurationException("Unable to obtain the locally assigned IP address, reason: " + ex.getMessage());
                        }
                    }
                    else
                    {
                        if (containsKey(SERVER_USE_SPECIFIED_IP_KEY))
                        {
                            // FIXME! validate the IP address
                            //
                            return new InetSocketAddress(getProperty(SERVER_USE_SPECIFIED_IP_KEY), port);
                        }

                        throw new ServerConfigurationException("Missing '" + SERVER_USE_SPECIFIED_IP_KEY + "' configuration entry");
                    }
                }

                throw new ServerConfigurationException("Missing '" + SERVER_USE_ASSIGNED_IP_KEY + "' configuration entry");

            }
            catch (final NumberFormatException ex)
            {
                throw new ServerConfigurationException("Non numeric server port number: '" + rawPort + "'");
            }
        }

        throw new ServerConfigurationException("Missing '" + SERVER_PORT_NUMBER_KEY + "' configuration entry");
    }

    // notes 1, only adding this override to save having to cast from Object when calling from client code
    //       2, the default shallow copy is fine as the referenced String key/value instances are immutable
    //
    @Override
    public Endpoints clone()
    {
        return (Endpoints)super.clone();
    }

    //
    // required by the Endpoints interface
    //

    public final List<SocketAddress> getConnections()
    {
        final var endpoints = new ArrayList<SocketAddress>();
        if (!containsKey(ENDPOINT_PREFIX_KEY + "1"))
        {
            logger.warn("No endpoints have been defined");

            return endpoints;
        }

        // note, the config specifies 3 server parameters and multiple endpoints
        //
        var error = false;
        for (var i = 1; i < (size() - 2); i++)
        {
            final var entry = getProperty(ENDPOINT_PREFIX_KEY + i);
            final var entryComponents = entry.split(":");
            try
            {
                error = (entryComponents.length != 2);
                if (!error)
                {
                    try
                    {
                        // FIXME! validate the IP address component
                        //
                        endpoints.add(new InetSocketAddress(entryComponents[0], Integer.parseInt(entryComponents[1])));
                    }
                    catch (final NumberFormatException ex)
                    {
                        error = true;
                    }
                }
            }
            finally
            {
                if (error)
                {
                    logger.error("An invalid configuration endpoint has been detected: '" + entry + "'");
                    logger.warn("An empty or partial endpoint configuration may have been returned");
                    break;
                }
            }
        }

        return endpoints;
    }
}
