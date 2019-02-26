import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

public class Engine extends JComponent {
    static List<Card> deck = new ArrayList<>();
    static Player[] players = new Player[8];
    static JFrame frame = new JFrame("Maze");
    static JLabel listener = new JLabel();
    int handPot=0;
    int sabaccPot=0;
    int selectedCard = 0;
    boolean tradeAvailable = true;
    boolean drawAvailable = true;
    boolean interferenceAvailable = true;
    Engine instance;
    String activePhase;
    Scanner scanner = new Scanner(System.in);
    static List<Integer> idiotsArray = new ArrayList<>(){
        {
            add(0);
            add(2);
            add(3);
        }
    };

    public static void main(String[] args) {
        new Engine();
    }
    public Engine(){
        players[0] = new Player("Ascor");
        instance = this;
        activePhase="betting";
        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "draw");
        listener.getActionMap().put("draw", new drawAction(1));

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("T"), "trade");
        listener.getActionMap().put("trade", new tradeAction(selectedCard));

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("I"), "interference");
        listener.getActionMap().put("interference", new interferenceAction(selectedCard));

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("U"), "comeUp");
        listener.getActionMap().put("comeUp", new comeUpAction());

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "shiftRight");
        listener.getActionMap().put("shiftRight", new shiftSelectionRight());

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "shiftLeft");
        listener.getActionMap().put("shiftLeft", new shiftSelectionLeft());

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "increaseBet");
        listener.getActionMap().put("increaseBet", new increaseBet());

        listener.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "decreaseBet");
        listener.getActionMap().put("decreaseBet", new decreaseBet());

        frame.setSize(500, 500);
        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.add(listener);

        createDeck();
        Collections.shuffle(deck);

        createAI();
        deal();
        deal();
        callScores();
        players[0].printHand();
    }
    private class increaseBet extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e){
            players[0].currentBet+=50;
            frame.repaint();
        }
    }
    private class decreaseBet extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e){
            if(players[0].currentBet-50>=0){
                players[0].currentBet-=50;
            }
            frame.repaint();
        }
    }
    private class shiftSelectionLeft extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e){
            if(!(selectedCard-1<0)){
                selectedCard--;
                listener.getActionMap().put("interference", new interferenceAction(selectedCard));
                listener.getActionMap().put("trade", new tradeAction(selectedCard));
                frame.repaint();
            }
        }
    }

    private class shiftSelectionRight extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e){
            if(!(selectedCard+1>players[0].hand.size()-1)){
                selectedCard++;
                listener.getActionMap().put("interference", new interferenceAction(selectedCard));
                listener.getActionMap().put("trade", new tradeAction(selectedCard));
                frame.repaint();
            }
        }
    }
    private class comeUpAction extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e){
            comeUp();
        }
    }
    private class interferenceAction extends AbstractAction{
        int interferedCard;
        interferenceAction(int toPlace){
            this.interferedCard=toPlace;
        }
        @Override
        public void actionPerformed(ActionEvent e){
            if(interferenceAvailable) {
                players[0].placeInInterference(interferedCard);
                if(selectedCard>players[0].hand.size()-1){
                    selectedCard--;
                }
                players[0].printHand();
                interferenceAvailable = false;
                drawAvailable = false;
                frame.repaint();
            }
        }
    }
    private class tradeAction extends AbstractAction{
        int tradedCard;
        tradeAction(int toTrade){
            this.tradedCard=toTrade;
        }
        @Override
        public void actionPerformed(ActionEvent e){
            if(tradeAvailable) {
                deck.add(players[0].tradeCard(nextCard(), tradedCard));
                Collections.shuffle(deck);
                players[0].printHand();
                tradeAvailable=false;
                drawAvailable=false;
                frame.repaint();
            }
        }
    }
    public static void comeUp(){
        Player[] scores = players.clone();
        List<Player> sabaccs = new ArrayList<>();
        List<Player> bombouts = new ArrayList<>();
        List<Player> regularScores = new ArrayList<>();
        List<Player> idiotArrays = new ArrayList<>();
        for(Player current : scores){
            /*if(current.hand.size()==3) {
                List<Integer> values = current.valuesToArray();
                if(values.containsAll(idiotsArray)){
                    idiotArrays.add(current);
                }
            }*/
            if(current.calculateHand() < -23||current.calculateHand() > 23){
                bombouts.add(current);
            }else if(current.calculateHand()==23||current.calculateHand()==-23){
                sabaccs.add(current);
            }else{
                regularScores.add(current);
            }
        }
        Collections.sort(regularScores);
        if(sabaccs.size()>1){
            int random = (int)(Math.random()*sabaccs.size());
            System.out.println("RNGESUS has decided to crown " + sabaccs.get(random).name + " as the winner");
        }else if(!sabaccs.isEmpty()){
            System.out.println(sabaccs.get(0).name + " is the winner with a sabacc");
        }else if(regularScores.get(regularScores.size()-1).calculateHand()!=regularScores.get(regularScores.size()-2).calculateHand()){
            System.out.println(regularScores.get(regularScores.size()-1).name + " won with the highest score.");
        }else{
            List<Player> tieBreaker = new ArrayList<>();
            for(Player current : regularScores){
                if(current.calculateHand()==regularScores.get(regularScores.size()-1).calculateHand()){
                    tieBreaker.add(current);
                }
            }
            int diceRoll = (int)(Math.random()*tieBreaker.size());
            System.out.println("RNGESUS has decided to crown " + tieBreaker.get(diceRoll).name);
        }
        /*Arrays.sort(scores);
        if(scores[scores.length-1]==scores[scores.length-2]){
            System.out.println("It's a tie!");
        }else{
            System.out.println(scores[scores.length-1].name + " has won!");
        }*/
    }
    private class drawAction extends AbstractAction{
        int toDraw;
        drawAction(int amnt){
            this.toDraw=amnt;
        }
        @Override
        public void actionPerformed(ActionEvent e){
            if(activePhase=="drawing") {
                if (drawAvailable) {
                    for (int cnt = 1; cnt <= toDraw; cnt++) {
                        players[0].drawCard(nextCard());
                    }
                    drawAvailable=false;
                    activePhase="betting";
                }
                players[0].printHand();
                frame.repaint();
            }
        }
    }
    static void callScores(){
        for(Player current : players){
            System.out.println(current.name + " " + current.calculateHand());
        }
    }

    static void deal(){
        for(int i = 0;i<=7;i++){
            players[i].drawCard(nextCard());
        }
    }

    static void createAI(){
        for(int i=1;i<8;i++){
            players[i] = new Player("AI "+i);
        }
    }

    static Card nextCard(){
        return deck.remove(deck.size()-1);
    }

    static Card[] createSuit(String s) {
        Card[] suit = new Card[15];
        for (int i = 1; i <= 15; i++) {
            Card current = new Card(i, s);
            suit[i - 1] = current;
        }
        return suit;
    }

    static Card[] createSpecials() {
        Card balance = new Card("Balance", -11);
        Card idiot = new Card("Idiot", 0);
        Card endurance = new Card("Endurance", -8);
        Card moderation = new Card("Moderation", -14);
        Card evilOne = new Card("Evil One", -15);
        Card queenOfAirAndDarkness = new Card("Queen of Air and Darkness", -2);
        Card demise = new Card("Demise", -13);
        Card star = new Card("Star", -17);
        if (deck.contains(star)) {
            Card altStar = new Card("Star", -10);
            return new Card[]{balance, idiot, endurance, moderation, evilOne, queenOfAirAndDarkness, demise, altStar};
        } else {
            return new Card[]{balance, idiot, endurance, moderation, evilOne, queenOfAirAndDarkness, demise, star};
        }
    }

    static void createDeck() {
        addToDeck(createSuit("Flasks"));
        addToDeck(createSuit("Sabers"));
        addToDeck(createSuit("Staves"));
        addToDeck(createSuit("Coins"));
        addToDeck(createSpecials());
        addToDeck(createSpecials());
    }

    static void addToDeck(Card[] toAdd) {
        for (Card current : toAdd) {
            deck.add(current);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(int i = 0; i<players[0].hand.size();i++){
            if(selectedCard==i){
                g.setColor(Color.blue);
            }else{
                g.setColor(Color.black);
            }
            g.drawString(players[0].hand.get(i).name, 100, 100 + (i*20));
        }
        g.drawString(Integer.toString(players[0].currentBet),200,100);
    }
}
