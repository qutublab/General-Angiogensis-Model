# Declare the all target as phony to prevent a conflict if a file named "all" is used.
.PHONY: all

JAVAC = javac

MASON = ../../mason
POIDIR = ../../poi-3.8-beta3
POIOOXML = $(POIDIR)/poi-ooxml-3.8-beta3-20110606.jar
POI = $(POIDIR)/poi-3.8-beta3-20110606.jar


# File names for shared package
Shared = CellInterface EnvironmentInterface LogStreamInterface MyStreamTokenizer NodeInterface Point3D RandomInterface RuleSetInterface GridInterface OutputBufferInterface Point3D Parameter StateDiagramModel StateDiagramModelResult BasicStats OneRepStats SimulationStats Shape CellGeometry  UserRequest

# File names for sharedmason package
SharedMason = ConcentrationsInterface

# File names for spheroid package
Spheroid = Spheroid Sphere

# File names for angiogensis package
Angiogenesis = SimpleRuleSet RuleSetMPB RuleSetPMB RuleSetMPMB ApoptosisRule BranchingRule ProliferationRule MigrationRule ActivationRule Storage Parameters MigrationRule2 Parameters2 RuleSetM2PB RuleSetPM2B RuleSetM2PM2B StateMachineRuleSet StateMachineRuleStorage StateMachineRuleSet2 RuleResult MigrationRule2B StateMachineRuleSet2B

#AngiogenesisPMB = RuleSet ApoptosisRule BranchingRule ProliferationRule MigrationRule ActivationRule Storage 

#AngiogenesisMPMB = RuleSet ApoptosisRule BranchingRule ProliferationRule MigrationRule ActivationRule Storage 

# File names for concentrations package
Concentrations = ConcentrationsManager Parameters

# File names for scaffold package
Scaffold = Environment Parameters Cell LogStream Node SimpleGrid LazySimpleGrid AbstractSprout OutputBuffer GrowthCapture Parameters GenSearch SproutData
#TestRepetitions

# File names for search package
Search = GenAlg

# File names for gui
Gui = ArcBall Renderable SimulationGUI GeneralRenderer EventAndListener SimulationRenderer Intersection SimulationIntersection Geometry

# File names for tools package
Tools = SpreadsheetWriter NotesWriter StateDiagramModelEditor Extractor


UNAME = $(shell uname)
ifeq ($(UNAME), Linux)
COLON = :
else
QUOTE = "
COLON = ;
endif


# Create all packages
all: $(MASON) shared.jar sharedMason.jar spheroid.jar angiogenesis.jar  concentrations.jar scaffold.jar search.jar tools.jar


# Create shared jar file
shared.jar: $(patsubst %, shared/%.class, $(Shared))
	jar -vcf0 shared.jar shared

shared/%.class: shared/%.java
	$(JAVAC) $<

# Create sharedMason jar file
sharedMason.jar: $(patsubst %, sharedMason/%.class, $(SharedMason))
	jar -vcf0 sharedMason.jar sharedMason

sharedMason/%.class: sharedMason/%.java $(MASON) shared.jar
	$(JAVAC) -classpath $(QUOTE).$(COLON)$(MASON)$(COLON)shared.jar$(QUOTE) $<

# Create spheroid jar file
spheroid.jar: $(patsubst %, spheroid/%.class, $(Spheroid))
	jar -vfc0 spheroid.jar spheroid

spheroid/%.class: spheroid/%.java $(MASON) shared.jar sharedMason.jar
	$(JAVAC) -classpath $(QUOTE).$(COLON)$(MASON)$(COLON)shared.jar$(COLON)sharedMason.jar $(QUOTE) $<


# Create search jar file
search.jar: $(patsubst %, search/%.class, $(Search))
	jar -vcf0 search.jar search

search/%.class: search/%.java shared.jar
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar $(QUOTE) $<


# Create angiogenesis jar file
angiogenesis.jar: $(patsubst %, angiogenesis/%.class, $(Angiogenesis))
	jar -vcf0 angiogenesis.jar angiogenesis

angiogenesis/%.class: angiogenesis/%.java shared.jar
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar $(QUOTE) $<

# Create gui jar file
gui.jar: $(patsubst %, gui/%.class, $(Gui))
	jar -vcf0 gui.jar gui

gui/%.class: gui/%.java shared.jar
	$(JAVAC) -classpath $(QUOTE)gui/jogl.all.jar$(COLON)gui/gluegen-rt.jar$(COLON)gui/nativewindow.all.jar$(COLON)gui/vecmath.jar$(COLON)shared.jar$(COLON).$(QUOTE) $<




#angiogenesisPMB.jar: $(patsubst %, angiogenesisPMB/%.class, $(AngiogenesisPMB))
#	jar -vcf0 angiogenesisPMB.jar angiogenesisPMB
#
#angiogenesisPMB/%.class: angiogenesisPMB/%.java shared.jar
#	$(JAVAC) -classpath ".:shared.jar" $<


#angiogenesisMPMB.jar: $(patsubst %, angiogenesisMPMB/%.class, $(AngiogenesisMPMB))
#	jar -vcf0 angiogenesisMPMB.jar angiogenesisMPMB
#
#angiogenesisMPMB/%.class: angiogenesisMPMB/%.java shared.jar
#	$(JAVAC) -classpath ".:shared.jar" $<


# Create concentrations jar file
concentrations.jar: $(patsubst %, concentrations/%.class, $(Concentrations))
	jar -vcf0 concentrations.jar concentrations

concentrations/%.class: concentrations/%.java shared.jar $(MASON) sharedMason.jar
	$(JAVAC) -classpath $(QUOTE).$(COLON)$(MASON)$(COLON)shared.jar$(COLON)sharedMason.jar$(QUOTE) $<

# Create scaffold jar file
scaffold.jar: $(patsubst %, scaffold/%.class, $(Scaffold))
	jar -vcf0 scaffold.jar scaffold

scaffold/%.class: scaffold/%.java $(MASON) shared.jar sharedMason.jar spheroid.jar search.jar
	$(JAVAC) -classpath $(QUOTE).$(COLON)$(MASON)$(COLON)shared.jar$(COLON)sharedMason.jar$(COLON)spheroid.jar$(COLON)ang$(COLON)search.jar$(QUOTE) $<

# Create tools jar file
tools.jar: $(patsubst %, tools/%.class, $(Tools))
	jar -vcf0 tools.jar tools

tools/SpreadsheetWriter.class: tools/SpreadsheetWriter.java shared.jar $(POIOOXML) $(POI)
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar$(COLON)$(POIOOXML)$(COLON)$(POI)$(QUOTE) $<

tools/NotesWriter.class: tools/NotesWriter.java shared.jar $(POIOOXML) $(POI)
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar$(COLON)$(POIOOXML)$(COLON)$(POI)$(QUOTE) $<

tools/Extractor.class: tools/Extractor.java shared.jar $(POIOOXML) $(POI)
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar$(COLON)$(POIOOXML)$(COLON)$(POI)$(QUOTE) $<

tools/Covariance.class: tools/Covariance.java shared.jar $(POIOOXML) $(POI)
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar$(COLON)$(POIOOXML)$(COLON)$(POI)$(QUOTE) $<

tools/StateDiagramModelEditor.class: tools/StateDiagramModelEditor.java shared.jar 
	$(JAVAC) -classpath $(QUOTE).$(COLON)shared.jar$(QUOTE) $<



