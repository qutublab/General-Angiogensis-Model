
package shared;

import java.util.*; 

public interface GuiInterface {

    public void initialize(EnvironmentInterface e);

    public UserRequest step(LinkedList<CellGeometry> cellGeometryList,
			    boolean simulationFinished);


}