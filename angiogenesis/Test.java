
public class Test {

    public enum State {A, B, C};

    public void printStates() {
	for (State s : State.values()) {
	    System.out.println(s);
	}
    }

    public static void main(String[] args) {
	Test t = new Test();
	t.printStates();
    }

}