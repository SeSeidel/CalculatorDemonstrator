package Calculator.general;

import Calculator.tree_elements.PixelInput;
import Calculator.tree_elements.TreeEdge;
import Calculator.tree_elements.TreeNode;
import Calculator.tree_elements.TreePath;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Merger {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    private HashSet<TreePath> pathSet;                                                      // Entspricht jeweils einer Untersuchungsmenge D aus der Ausarbeitung!
    private ArrayList<Pair<TreeEdge, TreePath>> allRelEdgesWithPaths = new ArrayList<>();   // Die Mengen aller für D betrachteten Kanten und deren Pfade.
    private EquEdgesSet allEquEdgesActDecLvl;                                               // Die Gesamtheit der Mengen gleichwertiger Kanten über allen Pfaden der aktuell betrachteten Entscheidungsebene.
    private HashSet<EqualityTupel> fullEquTupel = new HashSet<>();                          // Entspricht der Menge aller Gleichwertigkeitstupel aus der Ausarbeitung (mit mehr als einem Pfad).
    private HashSet<HashSet<EqualityTupel>> nonConflictSets = new HashSet<>();

    public Merger(HashSet<TreePath> paths, EquEdgesSet allEquEdges){
        this.allEquEdgesActDecLvl = allEquEdges;
        this.pathSet = paths;
    }
    // Garantiert das korrekte Löschen von Gleichwertigkeitstupeln, da alle Pfade auch ihre zugehörigen Tupel kennen müssen.
    private void removeEquTupel(EqualityTupel tupel){
        tupel.clearPaths();
        fullEquTupel.remove(tupel);
    }
    public TreeNode buildNewSubTree(){
        createRelEquTupel();                //TestContainer.equTupelTest(fullEquTupel); // Punkt 3.
        if (pathSet.size() > 1){
            preventTreeLoops();             //TestContainer.plausiblTestLoopPrevent(fullEquTupel); // Punkt 4.
            createNonConflictSets();        //TestContainer.nonConflictSetsTest(nonConflictSets); // Punkt 5.
        }
        chooseBestNonConflictSet();         //TestContainer.chooseBestnonConflictSetTest(nonConflictSets); // Punkt 6.
        TreeNode subTreeRoot = buildGraphForMerger();   // Punkt 7. mit Punkt 8.
        return subTreeRoot;
    }

    private void createRelEquTupel(){       // Punkt 3. des Kombinationsalgorithmus -> Beginn
        HashSet<Pair<TreeEdge, TreePath>> pairs = new HashSet<>();              // Prüfung der Mehrfachaufname gleicher Kanten nicht notwendig, wenn Pfade von unterschiedlichen Entscheidungen stammen!
        //HashSet<TreeEdge> processedEdges = new HashSet<>();                     // Kann folglich für dieses Verfahren entfernt werden!
        for (TreePath p: pathSet){
            for (TreeEdge e: p.getEdges()){
                //if (!processedEdges.contains(e)){                               // Kann folglich für dieses Verfahren entfernt werden!
                //    processedEdges.add(e);                                      // Kann folglich für dieses Verfahren entfernt werden!
                pairs.add(new Pair<>(e, p));
                allRelEdgesWithPaths.add(new Pair<>(e, p));
                //}
            }
        }
        if (pathSet.size() > 1){            // Wenn nur Kanten aus einem Pfad verarbeitet werden, besteht der Unterbaum auch nur aus einem Pfad
            while (!pairs.isEmpty()){
                Iterator<Pair<TreeEdge, TreePath>> pairIter = pairs.iterator();
                Pair<TreeEdge, TreePath> nextPair = pairIter.next();
                EqualityTupel nextNewT = new EqualityTupel(nextPair.getKey(),nextPair.getValue());
                pairIter.remove();
                while (pairIter.hasNext()){
                    nextPair = pairIter.next();
                    boolean toRemove = nextNewT.addNewPath(nextPair.getKey(), nextPair.getValue());
                    if (toRemove){
                        pairIter.remove();
                    }
                }
                if (nextNewT.numOfPaths() < 2) nextNewT.clearPaths();
                else fullEquTupel.add(nextNewT);
            }
        }
    }                                       // Ende Punkt 3.

    private void preventTreeLoops(){        // Punkt 4. des Kombinationsalgorithmus -> Beginn
        for (TreePath p: pathSet){
            if (p.getEdges().length == p.getConnEquTupel().size()){     // 4.1.
                subsetTest(p);
            }}
    }
    private void subsetTest(TreePath path){                             // Equivalent zu 4.2. ff.
        boolean overlaps = false;
        HashSet<EqualityTupel> testedTupelList = new HashSet<>();
        for (EqualityTupel t: path.getConnEquTupel()) {
            for (EqualityTupel listedT: testedTupelList){
                overlaps = !(listedT.getConnPaths().containsAll(t.getConnPaths()) || t.getConnPaths().containsAll(listedT.getConnPaths()));
            }
            if (overlaps) break;
            testedTupelList.add(t);
        }
        if (overlaps){
            EqualityTupel chosenMinTupel = selectMinConnEquTupel(path); // Minimaltupel mit den Pfaden in G sind Minimaltupel zu Pfad path
            HashSet<TreePath> setG = new HashSet<>();                   // 4.3. Aufstellen von G anhand Pfade im gewählten Minimaltupel
            for (TreePath tp: chosenMinTupel.getConnPaths()){
                if (tp.getConnEquTupel().equals(path.getConnEquTupel())) setG.add(tp);
            }
            EqualityTupel copyMinTupel = chosenMinTupel.copyMe();       // Restliche Tle 4.4.
            HashSet<TreePath> setAntiG = copyMinTupel.getConnPaths(); setAntiG.removeAll(setG);
            chosenMinTupel.switchPaths(setG);
            copyMinTupel.switchPaths(setAntiG);
            if (chosenMinTupel.numOfPaths() < 2){ removeEquTupel(chosenMinTupel); } // Punkt 4.5.
            if (copyMinTupel.numOfPaths() < 2){ copyMinTupel.clearPaths(); }
            else fullEquTupel.add(copyMinTupel);
        }
    }
    private EqualityTupel selectMinConnEquTupel(TreePath path){         // Tupel mit minimalen Pfaden aus 4.4 -> Tle 4.4.
        Iterator<EqualityTupel> tupelIter = path.getConnEquTupel().iterator();
        EqualityTupel minTupel = null;
        EqualityTupel altTupel;
        if (tupelIter.hasNext()) minTupel = tupelIter.next();
        while (tupelIter.hasNext()){
            altTupel = tupelIter.next();
            if (altTupel.numOfPaths() < minTupel.numOfPaths()) minTupel = altTupel;
        }
        return minTupel;
    }                                       // Ende Punkt 4.

    private void createNonConflictSets(){   // Punkt 5. des Kombinationsalgorithmus -> Beginn
        Iterator<EqualityTupel> equTupelIter = fullEquTupel.iterator();
        HashSet<EqualityTupel> firstSet = new HashSet<>();
        if (equTupelIter.hasNext()) {           // 5.1. wird nur zu Beginn einmal aufgerufen.
            firstSet.add(equTupelIter.next());
            nonConflictSets.add(firstSet);
        }
        allocateEquTupelToNonConflictSets(equTupelIter);                // Ablauf ab 5.2. mit restlichen Tupeln.
    }
    private void allocateEquTupelToNonConflictSets(Iterator<EqualityTupel> equTupelIter){
        while(equTupelIter.hasNext()) {
            HashSet<HashSet<EqualityTupel>> listOfFKSets = new HashSet<>();
            boolean noFKEqualedK = true;
            EqualityTupel testedTupel = equTupelIter.next();
            for (HashSet<EqualityTupel> cS : nonConflictSets) {                           // cS entspricht K gem. Ausarbeitung
                HashSet<EqualityTupel> setFK = calcNonConflictTupel(cS, testedTupel);     // 5.2.
                if (setFK.size() == cS.size()) {     // 5.2.4. und 5.2.6. und Marker für 5.3. - Prüfung auf Anzahl enthaltener Tupel reicht hier. - Tupel noch nicht in cS und setFK
                    noFKEqualedK = false;            // testedTupel kann konfliktfrei in aktuelles cS integriert werden!
                    cS.add(testedTupel);             // entspricht addieren zu setFK und anschließend setFK = cS, welches bereits in nonConflictSets ist
                }
                else if (!setFK.isEmpty()) {         // 5.2.5. und 5.2.6. - Prüfung auf Anzahl enthaltener Tupel reicht hier. - Tupel noch nicht in cS und setFK
                    setFK.add(testedTupel);
                    listOfFKSets.add(setFK);
                }
            }
            if (listOfFKSets.isEmpty() && noFKEqualedK) {
                HashSet<EqualityTupel> setFK = new HashSet<>();
                setFK.add(testedTupel);
                nonConflictSets.add(setFK);
            }
            for (HashSet<EqualityTupel> cSNew : nonConflictSets){  // Neue Mengen FK (inkl. überprüftem Tupel), die Teilmengen einer bereits bestehenden Menge K sind, könnn wegrationalisiert werden.
                for (Iterator<HashSet<EqualityTupel>> fKIter = listOfFKSets.iterator(); fKIter.hasNext(); ) {
                    HashSet<EqualityTupel> nextFK = fKIter.next();
                    if (cSNew.containsAll(nextFK)) fKIter.remove();
                }
            }
            nonConflictSets.addAll(listOfFKSets);
        }

    }
    private HashSet<EqualityTupel> calcNonConflictTupel(HashSet<EqualityTupel> tupelSetK, EqualityTupel tupel){
        HashSet<EqualityTupel> out = new HashSet<>(tupelSetK);               // Kopieren der aktuell betrachteten Menge K als Arbeitmenge für FK
        HashSet<EqualityTupel> iterationSet = new HashSet<>(tupelSetK);      // Arbeitsmenge zum iterieren, um K (tupelSet) parallel verändern zu können. (Ginge auch mit Iterator.remove u.s.w.)
        for (EqualityTupel t: iterationSet) {                               // 5.2.1.
            HashSet<TreePath> testSet = new HashSet<>(t.getConnPaths()); testSet.retainAll(tupel.getConnPaths());   // Erstelle jeweils die Schnittmenge der Pfade
            int testSize = testSet.size();
            if ((!testSet.isEmpty()) && testSize!=t.getConnPaths().size() && testSize!= tupel.getConnPaths().size()){
                out.remove(t);              // Wenn ein Pfadkonflikt vorliegt, entferne Tupel t aus Teilmenge konfliktfreier Tupel im betrachteten K (cS).
                if (testSize > 1) {         // 5.2.2.
                    EqualityTupel toAdd = new EqualityTupel(tupel.getRevEdge()); toAdd.switchPaths(testSet);
                    tupelSetK.add(toAdd); // prüfen, ob gem. Ausarbeitung zu out.add(toAdd) zu ändern ist
                }
                if (tupel.numOfPaths()-testSize > 1){   // 5.2.3.
                    EqualityTupel toAdd = new EqualityTupel(tupel.getRevEdge());
                    HashSet<TreePath> pathToAdd = new HashSet<>(tupel.getConnPaths()); pathToAdd.removeAll(testSet);    // Hinreichend die Schnittmenge zu entfernen.
                    toAdd.switchPaths(pathToAdd);
                    tupelSetK.add(toAdd); // prüfen, ob gem. Ausarbeitung zu out.add(toAdd) zu ändern ist
                }
            }
        }
        return out;     // Jeweils fertig berechnete Menge FK.
    }                   // Ende Punkt 5.

    private void chooseBestNonConflictSet(){    // Punkt 6. des Kombinationsalgorithmus mit Anfang/Auswahl aus Punkt 7.
        HashSet<EqualityTupel> best = new HashSet<>();
        int actPotential = 0;
        for (HashSet<EqualityTupel> cS: nonConflictSets){
            int newPotential = 0;
            for (EqualityTupel t: cS) newPotential = newPotential + t.numOfPaths()-1;
            if (newPotential > actPotential){
                actPotential = newPotential;
                best = cS;
            }}
        nonConflictSets.clear();
        nonConflictSets.add(best);
    }                   // Ende Punkt 6. inkl. Wahl der optimalen konfliktfreien Menge

    private TreeNode buildGraphForMerger(){         // Punkt 7. nach Wahl der optimalen konfliktfreien Menge
        ArrayList<Pair<TreeEdge, TreePath>> edgesToProcess = new ArrayList<>(allRelEdgesWithPaths);         // Kopie der Menge zur Kantenlöschung nach 7.1.2.
        HashSet<Pair<TreeNode, HashSet<TreePath>>> nodesWithFreePaths = new HashSet<>();                    // Zwischenspeicher von Knoten mit freien Ausgangspfaden nach 7.1 und 7.2.
        Pair<TreeNode, HashSet<TreePath>> mergerRoot = new Pair<>(new TreeNode(), pathSet);                 // Den Ursprungsknoten und seine Pfade vorbereiten.
        nodesWithFreePaths.add(mergerRoot);                                                                 // Den Ursprungsknoten als Startknoten setzen.
        Pair<TreeNode, HashSet<TreePath>> actPredNode;                                                      // Zw.-Speicher für Vorgängerknoten der aktuell betrachteten Referenzkante.
        if (!nonConflictSets.isEmpty()){                                                                    // Wenn es konfliktfreie Gleichwertigkeitstupel zu verarbeiten gibt...
            List<EqualityTupel> orderedList = new ArrayList<>(nonConflictSets.iterator().next());           // Ordnet die Tupel in einer gesonderten Liste "orderedList" mit den
            Collections.sort(orderedList, Comparator.comparingInt(EqualityTupel::numOfPaths).reversed());   // Tupeln nach "Pfadmächtigkeit" absteigend geordnet -> Tle 7.1.1.
            for (EqualityTupel tupel: orderedList){                                     // 7.1.1. und durch geordnete Abarbeitung 7.1.4.
                actPredNode = getRelevantNode(nodesWithFreePaths, tupel);               // 7.1.2.
                HashSet<Pair<TreeEdge, TreePath>> edgePairsToRemove = new HashSet<>();  // Etwas Speicher für etwas Laufzeit!
                for (Pair<TreeEdge, TreePath> e: edgesToProcess){                       // Identifiezieren der relevanten Kanten, die durch das aktuell betrachtete Tupel erfasst sind.
                    if (e.getKey().equalEdge(tupel.getRevEdge())) edgePairsToRemove.add(e);
                }
                edgesToProcess.removeAll(edgePairsToRemove);                            // Löschen der identifizierten Kanten aus den noch einzuarbeitenden Kanten.
                TreeEdge edgeCopy = processNewNode(tupel.getRevEdge(), tupel.getConnPaths(), actPredNode, nodesWithFreePaths);
                actPredNode.getValue().removeAll(tupel.getConnPaths());                 // Tle. 7.1.3.
                actPredNode.getKey().addSingleEdge(edgeCopy);                           // Referenzkante des Tupels an aktuellen Vorgängerknoten anhängen. -> restliche Tle. 7.1.3.
                incorporateMergedEdgesPaths(edgeCopy);
            }
        }
        for (Pair<TreeEdge, TreePath> e: edgesToProcess){                               // Punkt 7.2.
            actPredNode = getRelevantNode(nodesWithFreePaths, e.getValue());
            edgesToProcess.remove(e.getKey());
            actPredNode.getValue().remove(e.getValue());                                // 7.2.2.
            HashSet<TreePath> newSinglePathSet = new HashSet<>(); newSinglePathSet.add(e.getValue());
            TreeEdge edgeCopy = processNewNode(e.getKey(), newSinglePathSet, actPredNode, nodesWithFreePaths);
            actPredNode.getKey().addSingleEdge(edgeCopy);                               // 7.2.1.
            incorporateMergedEdgesPaths(edgeCopy);
        }                                                                               // Endknotenbehandlung nur bei DecLevel 1 notwendig und erfolgt durch das NeuralNet!
        return mergerRoot.getKey();
    }
    /**
     * Wählt aus einer Menge Knoten mit freien Ausgangspfaden jenen aus, der alle Pfade des angegebenen Gleichwertigkeits-Tupels als freie Pfade besitzt. Benötigt für 7.1.
     */
    private Pair<TreeNode, HashSet<TreePath>> getRelevantNode(HashSet<Pair<TreeNode, HashSet<TreePath>>> nodes, EqualityTupel tupel){ // Zur Einordnung der Tupelgebundenen Kanten.
        for (Pair<TreeNode, HashSet<TreePath>> n: nodes){
            if (n.getValue().containsAll(tupel.getConnPaths())) return n;
        }
        logger.error("An EqualityTupel did not fit into the available paths!"); // Sollte niemals auftreten wenn alles korrekt verarbeitet wurde!
        return null;
    }
    /**
     * Wählt aus einer Menge Knoten mit freien Ausgangspfaden jenen aus, der den Pfad der angegebenen Kante als freien Pfad besitzt. Benötigt für 7.2.
     */
    private Pair<TreeNode, HashSet<TreePath>> getRelevantNode(HashSet<Pair<TreeNode, HashSet<TreePath>>> nodes, TreePath edgePath){ // Zur Einordnung der Kanten ohne gleichwertige Gegenstücke.
        for (Pair<TreeNode, HashSet<TreePath>> n: nodes){
            if (n.getValue().contains(edgePath)) return n;
        }
        logger.error("An EqualityTupel did not fit into the available paths!"); // Sollte niemals auftreten wenn alles korrekt verarbeitet wurde!
        return null;
    }

    /**
     * Werzeugt eine Kopie von predEdge mit einem neuen TreeNode als Nachfolgeknoten, der alle connPaths als ausgehende Pfade besitzt und zu nodesWithFreePaths hinzugefügt wird.
     * Der actPredNode wird aus nodesWithFreePaths gelöscht, wenn seine ausgehenden Verbindungen alle im Graphen des Entscheidungsbaumes erfasst sind.
     * @param predEdge Referenzkante des aktuell zu verarbeitden Gleichwertigkeitstupels, die aus dem actPredNode ausgehen soll.
     * @param connPaths Pfade die im EqualityTupel des predEdge im Attribut connPaths gespeichert sind.
     * @param actPredNode Vorgöngerknoten des predEdge im aufzustellenden Entscheidungsbaum-Graphen.
     * @param nodesWithFreePaths Alle aktuellen Knoten in nodesWithFreePaths.
     * @return Ein Pair mit einer Kopie von predEdge als Key und einem neuen, korrekten verknüpften Nachfolgeknoten für PredEdge im Value.
     */
    private TreeEdge processNewNode(TreeEdge predEdge, HashSet<TreePath> connPaths, Pair<TreeNode, HashSet<TreePath>> actPredNode, HashSet<Pair<TreeNode, HashSet<TreePath>>> nodesWithFreePaths){ // Hilfsmethode für 7.1.3. und 7.2.2.
        Pair<TreeNode, HashSet<TreePath>> newMergerNode = new Pair<>( new TreeNode(), connPaths);
        TreeEdge out = predEdge.SemiShallowCopyForTreeNodes(newMergerNode.getKey());
        nodesWithFreePaths.add(newMergerNode);                                  // Endknotenmarkierung erfolgt durch Verbleib in nodesWithFreePaths am Ende des Teilalgorithmus (Ende 7.2.2.)
        if (actPredNode.getValue().isEmpty()) nodesWithFreePaths.remove(actPredNode);
        return out;
    }   // Ende Punkt 7.

    private void incorporateMergedEdgesPaths(TreeEdge edge){    // Punkt 8.
        HashSet<HashSet<TreeEdge>> potEquEdges = allEquEdgesActDecLvl.getNeuronRelatedEdges(edge.getSourceNeuronNum(), edge.getSourceNeuronFilterNum());
        for (HashSet<TreeEdge> equSet: potEquEdges) if (equSet.iterator().next().equalEdge(edge)) addCorrespPaths(edge, equSet);    // Wenn die Kanten in equSet gleichwertig zu edge sind werden deren Pfade für edge übernommen.
    }
    private void addCorrespPaths(TreeEdge actEdge, HashSet<TreeEdge> actEquSet){    // Passt!
        for (TreeEdge otherEdge: actEquSet){
            for (TreePath pathToAdd: otherEdge.getAllPaths()) actEdge.addPathWithoutInp(pathToAdd);     // Zusätzliche  Pfade ohne Erweiterung der CorrespondingInputs übernehmen.
        }                                                                                          // Die entstehenden zusätzlichen Eingabekombinationen werden später bottom-up in relInputVariantsForTree gespeichert.
        if (actEquSet.size() > 1 && actEquSet.iterator().next().getNumOfPaths()==0) {
            for (TreeEdge otherEdge: actEquSet){
                for (HashSet<PixelInput> pixInp: otherEdge.getRelInputVariantsForTree()){ actEdge.addRelInputVariant(pixInp); }
            }}
    }   // Ende Punkt 8.
}
