package student;

import model.*;

import java.util.*;

public class MyComputer implements Player {
    private Baron baron;
    private int score;
    private ArrayList<Route> routes = new ArrayList<>();
    private HashMap<String, MyStation> stations;
    private ArrayList<PlayerObserver> observers = new ArrayList<>();
    private Pair pair;
    private ArrayList<Card> hand = new ArrayList<>();
    private int pieces;
    private boolean claimedTurn;
    private LonelyEditionBarons game;

    /**
     * Creates a new player and initilize it
     *
     * @param baron
     */

    public MyComputer(Baron baron, LonelyEditionBarons game) {
        this.pieces = 45;
        this.game = game;
        this.baron = baron;
        claimedTurn = false;
        stations = new HashMap<>();
        for (PlayerObserver p : observers) {
            p.playerChanged(this);
        }
    }

    /**
     * This is called at the start of every game to reset the player to its
     * initial state:
     * <ul>
     * <li>Number of train pieces reset to the starting number of 45.</li>
     * <li>All remaining {@link Card cards} cleared from hand.</li>
     * <li>Score reset to 0.</li>
     * <li>Claimed {@link Route routes} cleared.</li>
     * <li>Sets the most recently dealt {@link Pair} of cards to two
     * {@link Card#NONE} values.</li>
     * </ul>
     *
     * @param dealt The hand of {@link Card cards} dealt to the player at the
     *              start of the game. By default this will be 4 cards.
     */
    @Override
    public void reset(Card... dealt) {
        this.pieces = 45;
        hand.clear();
//        for(int i=0;i<20;i++) {
//            hand.add(Card.BLACK);
//        }
        stations.clear();
        score = 0;
        routes.clear();
        pair = new MyPair(Card.NONE, Card.NONE);
        for (PlayerObserver p : observers) {
            p.playerChanged(this);
        }
    }

    /**
     * Adds an {@linkplain PlayerObserver observer} that will be notified when
     * the player changes in some way.
     *
     * @param observer The new {@link PlayerObserver}.
     */
    @Override
    public void addPlayerObserver(PlayerObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Removes an {@linkplain PlayerObserver observer} so that it is no longer
     * notified when the player changes in some way.
     *
     * @param observer The {@link PlayerObserver} to remove.
     */
    @Override
    public void removePlayerObserver(PlayerObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * The {@linkplain Baron baron} as which this player is playing the game.
     *
     * @return The {@link Baron} as which this player is playing.
     */
    @Override
    public Baron getBaron() {
        return baron;
    }

    /**
     * Used to start the player's next turn. A {@linkplain Pair pair of cards}
     * is dealt to the player, and the player is once again able to claim a
     * {@linkplain Route route} on the {@linkplain RailroadMap map}.
     *
     * @param dealt a {@linkplain Pair pair of cards} to the player. Note that
     *              one or both of these cards may have a value of {@link Card#NONE}.
     */
    @Override
    public void startTurn(Pair dealt) {
        claimedTurn = false;
        this.pair = dealt;

        if (dealt.getFirstCard() != Card.NONE && dealt.getSecondCard() != Card.NONE) {
            this.hand.add(pair.getFirstCard());
            this.hand.add(pair.getSecondCard());

        }
        for (PlayerObserver p : observers) {
            p.playerChanged(this);
        }
    }

    public void autoClaim() {
        int max = ((MyRailRoadMap) game.getRailroadMap()).getLengthOfLongestUnclaimedRoute();
        if (getNumberOfPieces() < max) {
            max = getNumberOfPieces();
        }
        if ((pair.getFirstCard().equals(Card.NONE) || pair.getSecondCard().equals(Card.NONE)) &&
                !hasEnoughCards(max)) {
            while (!hasEnoughCards(max)) {
                max--;
            }
        }
        for (Route r : game.getRailroadMap().getRoutes()) {
            if (r.getBaron() == Baron.UNCLAIMED && canClaimRoute(r)) {

                if (r.getLength() == max) {
                    try {
                        game.claimRoute(r.getTracks().get(0).getRow(), r.getTracks().get(0).getCol());
                        break;
                    } catch (RailroadBaronsException ex) {
                        System.out.println(ex);
                    }
                }


            }
        }

    }

    /**
     * Returns the most recently dealt {@linkplain Pair pair of cards}. Note
     * that one or both of the {@linkplain Card cards} may have a value of
     * {@link Card#NONE}.
     *
     * @return The most recently dealt {@link Pair} of {@link Card Cards}.
     */
    @Override
    public Pair getLastTwoCards() {
        return pair;
    }

    /**
     * Returns the number of the specific kind of {@linkplain Card card} that
     * the player currently has in hand. Note that the number may be 0.
     *
     * @param card The {@link Card} of interest.
     * @return The number of the specified type of {@link Card} that the
     * player currently has in hand.
     */
    @Override
    public int countCardsInHand(Card card) {
        int result = 0;
        for (Card c : hand) {
            if (c.equals(card)) {
                result++;
            }
        }
        return result;
    }

    /**
     * Returns the number of game pieces that the player has remaining. Note
     * that the number may be 0.
     *
     * @return The number of game pieces that the player has remaining.
     */
    @Override
    public int getNumberOfPieces() {
        return pieces;
    }


    /**
     * gets if there are enough cards to play
     *
     * @param size
     * @return
     */
   private boolean hasEnoughCards(int size) {
        if (countCardsInHand(Card.WILD) > 0 && size > 1) {
            size--;
        }
        for (Card card : Card.values()) {
            if (!card.equals(Card.NONE) && !card.equals(Card.BACK) && !card.equals(Card.WILD)) {
                if (countCardsInHand(card) >= size) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * gets the cards to be used when claiming
     *
     * @param size
     * @return
     */
    private ArrayList<Card> getEnoughCards(int size) {
        ArrayList<Card> cards = new ArrayList<>();
        //see if enough without wild
        for (Card card : Card.values()) {
            if (!card.equals(Card.NONE) && !card.equals(Card.BACK) && !card.equals(Card.WILD)) {
                if (countCardsInHand(card) == size) {
                    for (int i = 0; i < size; i++) {
                        cards.add(card);
                    }
                    return cards;
                }
            }
        }
        for (Card card : Card.values()) {
            if (!card.equals(Card.NONE) && !card.equals(Card.BACK) && !card.equals(Card.WILD)) {
                if (countCardsInHand(card) > size) {
                    for (int i = 0; i < size; i++) {
                        cards.add(card);
                    }
                    return cards;
                }
            }
        }
        for (Card card : Card.values()) {
            if (!card.equals(Card.NONE) && !card.equals(Card.BACK) && !card.equals(Card.WILD)) {
                if (countCardsInHand(card) == size - 1 && countCardsInHand(Card.WILD) > 0) {
                    for (int i = 0; i < size - 1; i++) {
                        cards.add(card);
                    }
                    cards.add(Card.WILD);
                    return cards;
                }
            }
        }
        return cards;
    }

    /**
     * Returns true iff the following conditions are true:
     * <p>
     * <ul>
     * <li>The {@linkplain Route route} is not already claimed by this or
     * some other {@linkplain Baron baron}.</li>
     * <li>The player has not already claimed a route this turn (players
     * are limited to one claim per turn).</li>
     * <li>The player has enough {@linkplain Card cards} (including ONE
     * {@linkplain Card#WILD wild card, if necessary}) to claim the
     * route.</li>
     * <li>The player has enough train pieces to claim the route.</li>
     * </ul>
     *
     * @param route The {@link Route} being tested to determine whether or not
     *              the player is able to claim it.
     * @return True if the player is able to claim the specified
     * {@link Route}, and false otherwise.
     */
    @Override
    public boolean canClaimRoute(Route route) {
        return pieces >= route.getLength() && hasEnoughCards(route.getLength()) && !claimedTurn;
    }

    /**
     * Claims the given {@linkplain Route route} on behalf of this player's
     * {@linkplain Baron Railroad Baron}. It is possible that the player has
     * enough cards in hand to claim the route by using different
     * combinations of {@linkplain Card card}. It is up to the implementor to
     * employ an algorithm that determines which cards to use, but here are
     * some suggestions:
     * <ul>
     * <li>Use the color with the lowest number of cards necessary to
     * match the length of the route.</li>
     * <li>Do not use a wild card unless absolutely necessary (i.e. the
     * player has length-1 cards of some color in hand and it is the most
     * numerous card that the player holds).</li>
     * </ul>
     *
     * @param route The {@link Route} to claim.
     * @throws RailroadBaronsException If the {@link Route} cannot be claimed,
     *                                 i.e. if the {@link #canClaimRoute(Route)} method returns false.
     */
    @Override
    public void claimRoute(Route route) throws RailroadBaronsException {
        if (canClaimRoute(route)) {
            ArrayList<Card> cardArrayList = getEnoughCards(route.getLength());
            for (Card card : cardArrayList) {
                hand.remove(card);
            }
            claimedTurn = true;
            route.claim(baron);
            routes.add(route);
            addNode(route);
            pieces -= route.getLength();
            score += route.getPointValue();
            for (PlayerObserver p : observers) {
                p.playerChanged(this);
            }
        } else {
            throw new RailroadBaronsException("Cannot claim route");
        }
    }

    private void addNode(Route route) {
        Station origin = route.getOrigin();
        Station destination = route.getDestination();
        MyStation originNode, destNode;
        if (stations.containsKey(origin.getName())) {
            originNode = stations.get(origin.getName());
        } else {
            originNode = new MyStation(origin.getName(), new MySpace(origin.getRow(), origin.getRow()));
            stations.put(origin.getName(), originNode);
        }
        if (stations.containsKey(destination.getName())) {
            destNode = stations.get(destination.getName());
        } else {
            destNode = new MyStation(destination.getName(), new MySpace(destination.getRow(), destination.getRow()));
            stations.put(destination.getName(), destNode);
        }
        originNode.addNeighbor(destNode);
        destNode.addNeighbor(originNode);

    }


    /**
     * Returns the {@linkplain Collection collection} of {@linkplain Route
     * routes} claimed by this player.
     *
     * @return The {@link Collection} of {@linkplain Route Routes} claimed by
     * this player.
     */
    @Override
    public Collection<Route> getClaimedRoutes() {
        return routes;
    }

    /**
     * Returns the players current score based on the
     * {@linkplain Route#getPointValue() point value} of each
     * {@linkplain Route route} that the player has currently claimed.
     *
     * @return The player's current score.
     */

    public boolean isConnectedBFS(String start, String finish) {
        if (stations.containsKey(start) && stations.containsKey(finish)) {
            MyStation finishNode = stations.get(finish);
            Map<MyStation, MyStation> pred = pathBFS(start);
           // List<MyStation> buildPath = buildPathBFS(stations, start, finish);
            return pred.containsKey(finishNode);
        }
        return false;
    }

    private Map<MyStation, MyStation> pathBFS(String start) {
        MyStation startNode = stations.get(start);
        List<MyStation> queue = new LinkedList<>();
        queue.add(startNode);
        Map<MyStation, MyStation> visited = new HashMap<>();
        //   visited.add(startNode);
        while (!queue.isEmpty()) {
            MyStation curr = queue.remove(0);
            for (MyStation nbr : curr.getNeighbors()) {
                if (!visited.containsKey(nbr)) {
                    queue.add(nbr);
                    visited.put(nbr, curr);
                }
            }
        }
        return visited;
    }

    public static List<MyStation> buildPathBFS(Map<MyStation, MyStation> graph, String start, String finish) {
        Map<MyStation, MyStation> predecessor = new HashMap<>();
        MyStation startNode, finishNode;
        startNode = graph.get(start);
        finishNode = graph.get(finish);
        predecessor.put(startNode, null);
        Queue<MyStation> toVisit = new LinkedList<>();
        toVisit.offer(startNode);

        while (!toVisit.isEmpty() && !toVisit.peek().equals(finishNode)) {
            MyStation thisNode = toVisit.remove();
            thisNode.getNeighbors().forEach(nbr -> {
                if (!predecessor.containsKey(nbr)) {
                    predecessor.put(nbr, thisNode);
                    toVisit.offer(nbr);
                }
            });
        }
        if (toVisit.isEmpty()) {
            return null;
        } else {
            List<MyStation> path = new LinkedList<>();
            path.add(0, finishNode);
            MyStation node = predecessor.get(finishNode);
            while (node != null) {
                path.add(0, node); // Reverse the direction to start->finish
                node = predecessor.get(node);
            }
            return path;
        }
    }

    public void addScore(int add) {
        score += add;
        for (PlayerObserver p : observers) {
            p.playerChanged(this);
        }
    }

    @Override
    public int getScore() {
        return score;
    }

    /**
     * Returns true iff the following conditions are true:
     * <p>
     * <ul>
     * <li>The player has enough {@linkplain Card cards} (including
     * {@linkplain Card#WILD wild cards}) to claim a
     * {@linkplain Route route} of the specified length.</li>
     * <li>The player has enough train pieces to claim a
     * {@linkplain Route route} of the specified length.</li>
     * </ul>
     *
     * @param shortestUnclaimedRoute The length of the shortest unclaimed
     *                               {@link Route} in the current game.
     * @return True if the player can claim such a {@link Route route}, and
     * false otherwise.
     */
    @Override
    public boolean canContinuePlaying(int shortestUnclaimedRoute) {
        return hasEnoughCards(shortestUnclaimedRoute) && pieces >= shortestUnclaimedRoute;
    }

    /**
     * tostring
     *
     * @return
     */
    @Override
    public String toString() {
        return baron + " Computer";
    }


}
