package shared;

import java.io.*;

public class StateDiagramModel implements Serializable
{
    private static boolean toStringDisabled = false;

	//Tip cell state diagram
	public double tipQuiescentToQuiescent;
	public double tipQuiescentToMigration;
	public double tipQuiescentToProliferation;
	public double tipQuiescentToBranching;
	
	public double tipMigrationToQuiescent;
	public double tipMigrationToMigration;
	public double tipMigrationToProliferation;
	public double tipMigrationToBranching;
	
	public double tipProliferationToQuiescent;
	public double tipProliferationToMigration;
	public double tipProliferationToProliferation;
	public double tipProliferationToBranching;
	
	public double tipBranchingToQuiescent;
	public double tipBranchingToMigration;
	public double tipBranchingToProliferation;
	public double tipBranchingToBranching;
	
	//Stalk cell state diagram
	public double stalkElongationToQuiescent;
	public double stalkElongationToProliferation;
	public double stalkElongationToBranching;
	
	public double stalkProliferationToQuiescent;
	public double stalkProliferationToProliferation;
	public double stalkProliferationToBranching;
	
	public double stalkBranchingToQuiescent;
	public double stalkBranchingToProliferation;
	public double stalkBranchingToBranching;
	
	public double stalkQuiescentToQuiescent;
	public double stalkQuiescentToProliferation;
	public double stalkQuiescentToBranching;
	
	//Rear cell state diagram
    //	public double rearQuiescentToQuiescent;
    //	public double rearQuiescentToProliferation;
    //	public double rearQuiescentToBranching;
    //	
    //	public double rearProliferationToQuiescent;
    //	public double rearProliferationToProliferation;
    //	public double rearProliferationToBranching;
    //	
    //	public double rearBranchingToQuiescent;
    //	public double rearBranchingToProliferation;
    //	public double rearBranchingToBranching;
	

    public static void disableToString() {
	toStringDisabled = true;
    }

    public String toString() {
	if (toStringDisabled) {
	    return super.toString();
	}
	String returnString;
	returnString = "StateDiagramModel["
	    + "tipQuiescentToQuiescent=" + tipQuiescentToQuiescent
	    + ",tipQuiescentToMigration=" + tipQuiescentToMigration
	    + ",tipQuiescentToProliferation=" + tipQuiescentToProliferation
	    + ",tipQuiescentToBranching=" + tipQuiescentToBranching   
	    + ",tipMigrationToQuiescent=" + tipMigrationToQuiescent
	    + ",tipMigrationToMigration=" + tipMigrationToMigration
	    + ",tipMigrationToProliferation=" + tipMigrationToProliferation
	    + ",tipMigrationToBranching=" + tipMigrationToBranching
	    + ",tipProliferationToQuiescent=" + tipProliferationToQuiescent
	    + ",tipProliferationToMigration=" + tipProliferationToMigration
	    + ",tipProliferationToProliferation=" + tipProliferationToProliferation
	    + ",tipProliferationToBranching=" + tipProliferationToBranching
	    + ",tipBranchingToQuiescent=" + tipBranchingToQuiescent
	    + ",tipBranchingToMigration=" + tipBranchingToMigration
	    + ",tipBranchingToProliferation=" + tipBranchingToProliferation
	    + ",tipBranchingToBranching=" + tipBranchingToBranching
	    + ",stalkElongationToQuiescent=" + stalkElongationToQuiescent
	    + ",stalkElongationToProliferation=" + stalkElongationToProliferation
	    + ",stalkElongationToBranching=" + stalkElongationToBranching
	    + ",stalkProliferationToQuiescent=" + stalkProliferationToQuiescent
	    + ",stalkProliferationToProliferation=" + stalkProliferationToProliferation
	    + ",stalkProliferationToBranching=" + stalkProliferationToBranching
	    + ",stalkBranchingToQuiescent=" + stalkBranchingToQuiescent
	    + ",stalkBranchingToProliferation=" + stalkBranchingToProliferation
	    + ",stalkBranchingToBranching=" + stalkBranchingToBranching
	    + ",stalkQuiescentToQuiescent=" + stalkQuiescentToQuiescent
	    + ",stalkQuiescentToProliferation=" + stalkQuiescentToProliferation
	    + ",stalkQuiescentToBranching=" + stalkQuiescentToBranching
	    //	    + ",rearQuiescentToQuiescent=" + rearQuiescentToQuiescent
	    //	    + ",rearQuiescentToProliferation=" + rearQuiescentToProliferation
	    //	    + ",rearQuiescentToBranching=" + rearQuiescentToBranching
	    //	    + ",rearProliferationToQuiescent=" + rearProliferationToQuiescent
	    //	    + ",rearProliferationToProliferation=" + rearProliferationToProliferation
	    //	    + ",rearProliferationToBranching=" + rearProliferationToBranching
	    //	    + ",rearBranchingToQuiescent=" + rearBranchingToQuiescent
	    //	    + ",rearBranchingToProliferation=" + rearBranchingToProliferation
	    //	    + ",rearBranchingToBranching=" + rearBranchingToBranching
	    + "]";
	return returnString;
    }
	
}