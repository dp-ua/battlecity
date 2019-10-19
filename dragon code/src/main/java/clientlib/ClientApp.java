package clientlib;


public class ClientApp {

    private final static String URL = "http://dojorena.io/codenjoy-contest/board/player/r20m8inwejol5iawzw6m?code=4526161713293345680&gameName=battlecity";

    public static void main(String[] args) {
        try {
            WebSocketRunner.run(URL, new SampleSolver());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
