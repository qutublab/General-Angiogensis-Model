
package shared;


/*
 * Interface for methods of an agiogenic rule set Cell that can be
 * invoked by the simulation framework.
 *
 * Unfortunately interfaces do not allow constructors.  Abstract
 * classes allow constructors, but this would force classes to be
 * subclasses of the abstract class and useful inheritance of other
 * classes would be prohibited becuase Java does not allow multiple
 * inheritance.
 */

public interface RuleSetInterface {

    // short rule set name suitbale for including in a file name
    public String getRuleSetIdentifier();
    // long rule set name
    public String getRuleSetName();

    public void initialize(EnvironmentInterface e);

    public Object createLocalStorage(CellInterface c);

    public void actCell(CellInterface c,
			Object localStorage,
			EnvironmentInterface e);

    public boolean tipCellsHavePrecedence();


    public void setDebugFlag();

    public int getStateInt(CellInterface c);


    /*
    public void actNode(NodeInterface n,
			Object localStorage,
			EnvironmentInterface e);
    */

    /*
    public void migrateCell(double scale,
			    CellInterface c,
			    Object localStorage,
			    EnvironmentInterface e);
    */

    /*
    public void proliferateCell(double scale,
				CellInterface c,
				Object localStorage,
				EnvironmentInterface e);
    */
}
