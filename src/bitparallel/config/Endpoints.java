//
// (c) Bit Parallel Ltd, May 2025
//

package bitparallel.config;

import java.net.SocketAddress;
import java.util.List;

public interface Endpoints
{
    public List<SocketAddress> getConnections();
}
