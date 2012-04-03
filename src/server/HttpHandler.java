package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;

/**
 * A simplistic HTTP protocol handler that replies back the URL and headers
 * which a client requested.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 00:19:00 -0300 (Vie, 13 Jul 2007)
 *          $
 */
public class HttpHandler extends StreamIoHandler {

    private HttpServerHandler server;

    public HttpHandler(HttpServerHandler server) {
        this.server = server;
    }

    protected void processStreamIo(IoSession session, InputStream in,
            OutputStream out) {
        // You *MUST* execute stream I/O logic in a separate thread.
        new Worker(session, in, out).start();
    }

    private class Worker extends Thread {
        private final InputStream in;

        private final OutputStream out;

        // private final IoSession session;

        public Worker(IoSession session, InputStream in, OutputStream out) {
            setDaemon(true);
            // this.session = session;
            this.in = in;
            this.out = out;
        }

        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    this.in));
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(this.out)));

            try {
                // url -> kick?<user> | ban?<user> | unban?<user> | listban
                // String url = in.readLine().split(" ")[1].substring(1);

                StringBuffer response = new StringBuffer();

                // Write header
                response.append("HTTP/1.0 200 OK\n");
                response.append("Content-Type: text/plain\n");
                response.append("\n");

                // if (url.equalsIgnoreCase("listban")) {
                // List<String> bans = server.listBans();
                // for (String ban : bans) {
                // response.append(ban + "\n");
                // }
                // }
                // else if (url.indexOf("?") > 0) {
                // // asumo hay un ?
                // String action = url.substring(0, url.indexOf("?"));
                // String player = url.substring(url.indexOf("?") + 1);
                // InetSocketAddress sa = (InetSocketAddress) session
                // .getRemoteAddress();
                //
                // if (action.equalsIgnoreCase("kick")) {
                // server.kickPlayer(sa.getAddress().toString(), player, 5);
                // }
                // else if (action.equalsIgnoreCase("ban")) {
                // server.banPlayer(sa.getAddress().toString(), player);
                // }
                // else if (action.equalsIgnoreCase("unban")) {
                // server.unbanPlayer(sa.getAddress().toString(), player);
                // }
                //
                // response.append("ok");
                // }
                // else {
                // // Write content
                response.append(server.getUsersRooms());
                // }

                out.println(response.toString());
            }
            catch (Exception e) {
                out.println("HTTP/1.0 500 Internal Server Error");
                out.println("Content-Type: text/plain");
                out.println();

                e.printStackTrace(out);
            }
            finally {
                out.flush();
                out.close();
                try {
                    in.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
}