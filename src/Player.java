import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player implements Comparable<Player> {
    List<Card> hand;
    List<Card> interference;
    String name;
    int credits;
    int currentBet;

    Player(String n){
        this.name=n;
        this.hand = new CopyOnWriteArrayList<>();
        this.interference = new CopyOnWriteArrayList<>();
        this.credits=500;
    }

    void resetCards(){
        this.hand = new CopyOnWriteArrayList<>();
        this.interference = new CopyOnWriteArrayList<>();
    }

    int calculateHand(){
        int score = 0;
        for(Card current : hand){
            score+=current.value;
        }
        if(!interference.isEmpty()){
            score+=interference.get(0).value;
        }
        return score;
    }

    void printHand(){
        System.out.print("Your hand contains: ");
        Iterator<Card> heldCards = hand.iterator();
        while(heldCards.hasNext()){
            Card current = heldCards.next();
            if(heldCards.hasNext()){
                System.out.print(current.name + ", ");
            }else{
                System.out.println(current.name + ".");
                System.out.println("For a value of: " + calculateHand());
            }
        }
        if(!interference.isEmpty()){
            System.out.println("Cards in interference field: " + interference.get(0).name);
        }
        System.out.println();
    }

    void comeUp(){

    }

    void drawCard(Card drawn){
        hand.add(drawn);
    }

    Card tradeCard(Card drawn, int toGiveBack){
        hand.add(drawn);
        Card givenBack = hand.get(toGiveBack);
        hand.remove(toGiveBack);
        return givenBack;
    }

    void placeInInterference(int toPlace){
        hand.get(toPlace).inInterference=true;
        interference.add(hand.get(toPlace));
        hand.remove(toPlace);
    }
    List<Integer> valuesToArray(){
        List<Integer> values = new ArrayList<>();
        for(Card current : hand){
            values.add(current.value);
        }
        return values;
    }
    @Override
    public int compareTo(Player plr){
        return this.calculateHand()-plr.calculateHand();
    }
}
