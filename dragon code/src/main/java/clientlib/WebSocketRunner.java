package clientlib;


import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebSocketRunner {
    private static boolean printToConsole = true;
    private static Map<String, WebSocketRunner> clients = new ConcurrentHashMap<>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private WebSocket.Connection connection;
    private Solver solver;
    private WebSocketClientFactory factory;
    private Runnable onClose;

    private WebSocketRunner(Solver solver) {
        this.solver = solver;
    }

    public static void run(String url, Solver solver) {
        String serverLocation = url.replace("http", "ws").replace("board/player/", "ws?user=").replace("?code=", "&code=");

        final int LISTENERS_COUNT = 1;
        Thread[] threads = new Thread[LISTENERS_COUNT];
        for (int i = 0; i != LISTENERS_COUNT; ++i) {
            threads[i] = new Thread(() -> {
                try {
                    final WebSocketRunner client = new WebSocketRunner(solver);
                    client.start(serverLocation);
                    Runtime.getRuntime().addShutdownHook(new Thread(client::stop));
                    clients.put(serverLocation, client);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads[i].start();
        }

        for (int i = 0; i != LISTENERS_COUNT; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String formatData(String data) {
        data = data.replaceAll("☼", "#");
        data = data.replaceAll("•", "*");
        int size = (int) Math.sqrt(data.length() + 1);
        StringBuilder sb = new StringBuilder();
        int current = 0;
        System.out.println(data);
        while (current < data.length()) {
            sb.append(data, current, current += size);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void print(String message) {
        if (printToConsole) {
            System.out.println(sdf.format(new Date()) + " " + Thread.currentThread().getId() + " " + message);
        }
    }

    private void stop() {
        try {
            connection.close();
            factory.stop();
        } catch (Exception e) {
            print(e);
        }
    }

    private void start(final String server) throws Exception {
        final Pattern urlPattern = Pattern.compile("^board=(.*)$");

        factory = new WebSocketClientFactory();
        factory.start();

        final WebSocketClient client = factory.newWebSocketClient();

        onClose = () -> {
            printReconnect();
            connectLoop(server, urlPattern, client);
        };

        connectLoop(server, urlPattern, client);
    }

    private void connectLoop(String server, Pattern urlPattern, WebSocketClient client) {
        while (true) {
            try {
                tryToConnect(server, urlPattern, client);
                break;
            } catch (Exception e) {
                print(e);
                printReconnect();
            }
        }
    }

    private void printReconnect() {
        print("Waiting before reconnect...");
        sleep(5000);
    }

    private void tryToConnect(String server, final Pattern urlPattern, WebSocketClient client) throws Exception {
        URI uri = new URI(server);
        print(String.format("Connecting '%s'...", uri));

        if (connection != null) {
            connection.close();
        }

        connection = client.open(uri, new WebSocket.OnTextMessage() {
            public void onOpen(Connection connection) {
                print("Opened connection " + connection.toString());
            }

            public void onClose(int closeCode, String message) {
                if (onClose != null) {
                    onClose.run();
                }
                print("Closed with message: '" + message + "' and code: " + closeCode);
            }


            public void onMessage(String data) {
                try {
                    Matcher matcher = urlPattern.matcher(data);
                    if (!matcher.matches()) {
                        throw new RuntimeException("Error parsing data: " + data);
                    }

                    String answer = solver.makeAMove(matcher.group(1));
                    print("Sending step: " + answer);
                    connection.sendMessage(answer);
                } catch (Exception e) {
                    print(e);
                }
            }
        }).get(5000, TimeUnit.MILLISECONDS);
    }

    private void sleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            print(e);
        }
    }

    private void print(Exception e) {
        if (printToConsole) {
            System.out.println(sdf.format(new Date()) + " " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
