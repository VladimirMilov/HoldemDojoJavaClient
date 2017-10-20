package com.nedogeek;


import org.eclipse.jetty.websocket.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Collections;

public class Client {
    private static final String userName = "Vladi";
    private static final String password = "somePassword";

    private static final String SERVER = "ws://10.22.40.124:8080/ws";
    private org.eclipse.jetty.websocket.WebSocket.Connection connection;

    enum Commands {
        Check, Call, Rise, Fold, AllIn
    }

    class Card {
        final String suit;
        final String value;

        Card(String suit, String value) {
            this.suit = suit;
            this.value = value;
        }
    }


    private void con() {
        WebSocketClientFactory factory = new WebSocketClientFactory();
        try {
            factory.start();

        WebSocketClient client = factory.newWebSocketClient();

        connection = client.open(new URI(SERVER + "?user=" + userName + "&password=" + password), new org.eclipse.jetty.websocket.WebSocket.OnTextMessage() {
            public void onOpen(Connection connection) {
                System.out.println("Opened");
            }

            public void onClose(int closeCode, String message) {
                System.out.println("Closed");
            }

            public void onMessage(String data) {
                parseMessage(data);
                System.out.println(data);

                if (userName.equals(mover)) {
                    try {
                        doAnswer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).get(500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Player {

        final String name;
        final int balance;
        final int bet;
        final String status;
        final List<Card> cards;
        Player(String name, int balance, int bet, String status, List<Card> cards) {
            this.name = name;
            this.balance = balance;
            this.bet = bet;
            this.status = status;
            this.cards = cards;
        }

    }
    List<Card> deskCards;

    int pot;
    String gameRound;

    String dealer;
    String mover;
    List<String> event;
    List<Player> players;
    String cardCombination;


    public Client() {
            con();
    }

    public static void main(String[] args) {
        new Client();
    }

    private void parseMessage(String message) {
        JSONObject json = new JSONObject(message);

        if (json.has("deskPot")) {
            pot = json.getInt("deskPot");
        }
        if (json.has("mover")) {
            mover = json.getString("mover");
        }
        if (json.has("dealer")) {
            dealer = json.getString("dealer");
        }
        if (json.has("gameRound")) {
            gameRound = json.getString("gameRound");
        }
        if (json.has("event")) {
            event = parseEvent(json.getJSONArray("event"));
        }
        if (json.has("players")) {
            players = parsePlayers(json.getJSONArray("players"));
        }

        if (json.has("deskCards")) {
            deskCards = parseCards(((JSONArray) json.get("deskCards")));
        }

        if (json.has("combination")) {
            cardCombination = json.getString("combination");
        }
    }

    private List<String> parseEvent(JSONArray eventJSON) {
        List<String> events = new ArrayList<String>();

        for (int i = 0; i < eventJSON.length(); i++) {
            events.add(eventJSON.getString(i));
        }

        return events;
    }

    private List<Player> parsePlayers(JSONArray playersJSON) {
        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < playersJSON.length(); i++) {
            JSONObject playerJSON = (JSONObject) playersJSON.get(i);
            int balance = 0;
            int bet = 0;
            String status = "";
            String name = "";
            List<Card> cards = new ArrayList<Card>();

            if (playerJSON.has("balance")) {
                balance = playerJSON.getInt("balance");
            }
            if (playerJSON.has("pot")) {
                bet = playerJSON.getInt("pot");
            }
            if (playerJSON.has("status")) {
                status = playerJSON.getString("status");
            }
            if (playerJSON.has("name")) {
                name = playerJSON.getString("name");
            }
            if (playerJSON.has("cards")) {
                cards = parseCards((JSONArray) playerJSON.get("cards"));
            }

            players.add(new Player(name, balance, bet, status, cards));
        }

        return players;
    }

    private List<Card> parseCards(JSONArray cardsJSON) {
        List<Card> cards = new ArrayList<Card>();

        for (int i = 0; i < cardsJSON.length(); i++) {
            String cardSuit = ((JSONObject) cardsJSON.get(i)).getString("cardSuit");
            String cardValue = ((JSONObject) cardsJSON.get(i)).getString("cardValue");

            cards.add(new Card(cardSuit, cardValue));
        }

        return cards;
    }

    int gameRounds = 0;
    int bigBlind = 0;

    private void doAnswer() throws IOException {

        System.out.println(myPlayer().name);
        System.out.println(myPlayer().cards.get(0).suit + "  " + myPlayer().cards.get(0).value);
        System.out.println(myPlayer().cards.get(1).suit + "  " + myPlayer().cards.get(1).value);

//        gameRounds++;
//        if(gameRounds <= 20) {
//            System.out.println("GAME ROUNDS ARE: " + gameRounds);
//            connection.sendMessage(Commands.Fold.toString());
//            return;
//        }

        if(gameRound.equals("BLIND")) {
            preFlopStrategy();
        }

    }


    private void preFlopStrategy() throws IOException {

        String myCards = myPlayer().cards.get(0).value.toLowerCase().toString().concat(myPlayer().cards.get(1).value.toLowerCase().toString());

        for (Player p : players) {
            if (p.status.equals("BigBlind")) {
                bigBlind = p.bet;
            }
        }
        if(amIInGoodPosition()) {
            System.out.println("VERY GOOD POSITION");
        }
        List<String> allInCards = new ArrayList<String>();
        allInCards.add("aa");
        allInCards.add("kk");
        allInCards.add("qq");


        for(int i = 0; i < allInCards.size(); i++) {
            if(myCards.equals(allInCards.get(i)) || myCards.equals((new StringBuilder(allInCards.get(i)).reverse().toString()))) {
                System.out.println("INTO ALL IN CARDS---------------");
                connection.sendMessage(Commands.AllIn.toString());
                return;
            }
        }

        List<String> raiseCards = new ArrayList<String>();
        raiseCards.add("ak");
        raiseCards.add("aq");
        raiseCards.add("aj");
        raiseCards.add("at");
        raiseCards.add("kq");
        raiseCards.add("kj");
        raiseCards.add("qj");
        raiseCards.add("jt");
        raiseCards.add("jt");
        raiseCards.add("t9");

        for(int i = 0; i < raiseCards.size(); i++) {
            if(myCards.equals(raiseCards.get(i)) || myCards.equals((new StringBuilder(raiseCards.get(i)).reverse().toString()))) {
                if (howMuchHasSomeoneRaised(bigBlind) >= myPlayer().balance / 3) {
                    connection.sendMessage(Commands.Call.toString());
                    return;
                }
                System.out.println("INTO RAISE CARDS---------------");
                connection.sendMessage(Commands.Rise.toString() + "," + pot/2.5);
                return;
            }
        }

        List<String> callCards = new ArrayList<String>();
        callCards.add("qt");
        callCards.add("t8");
        callCards.add("t7");
        callCards.add("98");
        callCards.add("97");
        callCards.add("87");
        callCards.add("76");
        callCards.add("65");
        callCards.add("54");
        callCards.add("43");
        callCards.add("32");
        callCards.add("a2");
        callCards.add("24");
        callCards.add("35");
        callCards.add("46");
        callCards.add("57");
        callCards.add("68");
        callCards.add("79");
        callCards.add("8t");

        for(int i = 0; i < callCards.size(); i++) {
            if(myCards.equals(callCards.get(i)) || myCards.equals((new StringBuilder(callCards.get(i)).reverse().toString()))) {
                if (howMuchHasSomeoneRaised(bigBlind) >= myPlayer().balance / 3) {
                    connection.sendMessage(Commands.Fold.toString());
                    return;
                }
                System.out.println("INTO CALL CARDS---------------");
                connection.sendMessage(Commands.Call.toString());
                return;
            }
        }

        if (howMuchHasSomeoneRaised(bigBlind) != 0) {
            System.out.println("PREFLOP WILL CHECK---------------");
            connection.sendMessage(Commands.Call.toString());
            return;
        } else {
            System.out.println("PREFLOP WILL FOLD---------------");
            connection.sendMessage(Commands.Fold.toString());
            return;
        }

    }




    private void flopStrategy() throws IOException {
        if (howMuchHasSomeoneRaised(bigBlind) != 0) {
            if (amIInGoodPosition()) {
                connection.sendMessage(Commands.Rise.toString() + "," + bigBlind);
                return;
            }
        }


        List<String> allPossibleCardCombinations = new ArrayList<String>();
        allPossibleCardCombinations.add("High card");
        allPossibleCardCombinations.add("Pair of");
        allPossibleCardCombinations.add("Two pairs");
        allPossibleCardCombinations.add("Set of");
        allPossibleCardCombinations.add("Straight");
        allPossibleCardCombinations.add("Flash");
        allPossibleCardCombinations.add("Full house");
        allPossibleCardCombinations.add("Four of");

        for(int i = 0; i < allPossibleCardCombinations.size(); i++) {

        }


    }




//    private int positionOnTable() {
//        for (int i = 0; i < players.size(); i++) {
//            if (players.get(i).status.equalsIgnoreCase("smallblind")) {
////                p = i;
////            }
////        }
//            }
//        }
//    }

    private int howMuchHasSomeoneRaised(int bigBlind){
        int otherBetRaise = 0;
        for (Player p : players) {
            if (!p.name.equals(myPlayer().name)) {
                if (p.bet > bigBlind) {
                    otherBetRaise = p.bet;
                }
            }
        }
        return otherBetRaise;
    }

    // TODO fix this logic if you have time ->
    private boolean amIInGoodPosition() {
        if(dealer.equals(myPlayer().name)) {
            return true;
        }
        return false;
    }



    private Player myPlayer() {
        Player myPlayer = null;
        for(Player player : players) {
            if(player.name.equals(userName)) {
                myPlayer = player;
                break;
            }
        }
        return myPlayer;
    }

}
