//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.communications;

import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ConnectionHandler implements Runnable
{
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class);

    private final Socket socket;

    public ConnectionHandler(final Socket socket)
    {
        this.socket = socket;
    }

    public final void run()
    {
        logger.info("A client cconnection has been accepted from: " + socket);
    }
}
