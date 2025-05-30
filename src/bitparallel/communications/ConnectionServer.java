//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.communications;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.config.BridgeConfig;

public class ConnectionServer
{
    private final static int LISTEN_TIMEOUT_MS = 250;
    private final static Logger logger = LogManager.getLogger(ConnectionServer.class);

    private final Runnable serverTask;
    private final AtomicBoolean serverRunning;

    public ConnectionServer(final BridgeConfig config)
    {
        final var executor = Executors.	newCachedThreadPool();

        serverRunning = new AtomicBoolean(false);
        serverTask = () -> {
            try
            {
                logger.info("The TCP-UDP Server task has started");

                final var listenSocket = new ServerSocket();
                listenSocket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                listenSocket.setSoTimeout(LISTEN_TIMEOUT_MS);
                listenSocket.bind(config.getServerSocketAddress());
                logger.info("The ConnectionServer is listening on " + listenSocket.getLocalSocketAddress());

                while (serverRunning.get())
                {
                    try
                    {
                        final var connection = listenSocket.accept();
                        final var endpoints = config.clone().getConnections();
                        executor.submit(new ConnectionHandler(connection, endpoints));
                    }
                    catch (final SocketTimeoutException ignored)
                    {
                        //
                        // provides an opportunity for the loop to exit
                        //
                    }
                    catch (final Exception ex)
                    {
                        logger.error("Unable to handle a client connection");
                        logger.error(ex.getMessage());
                    }
                }

                listenSocket.close();
                logger.info("The TCP-UDP Server task is exiting...");
            }
            catch (final Exception ex)
            {
                logger.error("The ConnectionServer task has failed to start");
                logger.error(ex.getMessage());
            }
        };
    }

    public final void start()
    {
        final var serverThread = new Thread(serverTask, "UDP-TCP Server Task");
        serverRunning.set(true);
        serverThread.start();
    }

    public final void stop()
    {
        serverRunning.set(false);
    }
}
