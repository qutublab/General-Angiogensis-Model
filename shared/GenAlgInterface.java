package shared;
import java.util.*;

public interface GenAlgInterface {

    public int getIterationNumber();
	
	public void initialize(RandomInterface r);
	public ArrayList<StateDiagramModelResult> createPop();
	public StateDiagramModel updatePop(ArrayList<StateDiagramModelResult> r);
	//public StateDiagramModel cull(ArrayList<StateDiagramModelResult> a);
}
