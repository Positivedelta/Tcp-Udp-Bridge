//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.communications;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ConnectionHandler implements Runnable
{
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class);
    private final int ASCII_0 = 48;
    private final int ASCII_9 = 57;

    private final Socket socket;
    private final List<SocketAddress> endpoints;

    public ConnectionHandler(final Socket socket, final List<SocketAddress> endpoints)
    {
        this.socket = socket;
        this.endpoints = endpoints;
    }

    public final void run()
    {
        logger.info("A client cconnection has been accepted from: " + socket);

        try (final var is = socket.getInputStream(); final var os = socket.getOutputStream())
        {
            final var writer = new PrintWriter(os, true);
            writer.println();
            writer.println("Please select an endpoint");
            var endpointIndex = 1;
            for (final var socketAddress : endpoints)
            {
                final var endpoint = (InetSocketAddress)socketAddress;
                writer.print(String.format("%1$2s: ", Integer.toString(endpointIndex++)));
                writer.println(endpoint.getAddress().getHostAddress() + ":" + endpoint.getPort());
            }

            final var doPrompt = new AtomicBoolean(true);
            while (doPrompt.get())
            {
                writer.print("# ");
                writer.flush();

                final var selectedIndex = readIndex(is, endpoints.size());
                selectedIndex.ifPresentOrElse(rxedIndex -> {
                    doPrompt.set(false);

                    final var endpoint = (InetSocketAddress)endpoints.get(rxedIndex - 1);
                    logger.info("Client '" + socket + "' selected index " + rxedIndex + " (" + endpoint + ")");

                    //
                    // FIXME! open the endpoint and connect the streams...
                    //

                    while (true)
                    {
                        try {Thread.sleep(1000);} catch (final Exception ex) {};
                    }
                }, () -> {
                    writer.println("Invalid selection");
                });
            }
        }
        catch (final IOException ex)
        {
            logger.error("Unable to communicate with the client associated with " + socket);
            logger.error(ex.getMessage());
        }
    }

    private final Optional<Integer> readIndex(final InputStream is, final int limit) throws IOException
    {
        var index = 0;
        var invalid = false;
        while (true)
        {
            final var rxedByte = is.read();
            if ((rxedByte == '\r') || (rxedByte == '\n')) break;
            if ((rxedByte < ASCII_0) || (rxedByte > ASCII_9)) invalid = true;

            index = (index * 10) + rxedByte - ASCII_0;
        };

        // skip the extra byte if the line termination ends up being '\r\n' or '\n\r'
        //
        is.skip(is.available());

        return (invalid || (index == 0) || (index > limit)) ? Optional.empty() : Optional.of(index);
    }
}
