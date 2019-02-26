public class Card {
    int value;
    String name;
    boolean inInterference = false;

    Card(int v, String suit){
        this.value = v;
        this.name = v + " of " + suit;
    }
    Card(String name, int v){
        this.value = v;
        this.name = name;
    }
    void placeInInterference(){
        this.inInterference=true;
    }
}
