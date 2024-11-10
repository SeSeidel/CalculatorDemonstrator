package Calculator.general;

import Calculator.tree_elements.TreeEdge;
import Calculator.tree_elements.TreeNode;
import Calculator.tree_elements.TreePath;

import java.util.HashSet;
import java.util.Iterator;

public final class TestContainer {

    public static void allEquPathsTest(EquEdgesSet testSet) {
        System.out.println(" ");
        int m = 0;
        int n = 0;
        for (Iterator<HashSet<HashSet<TreeEdge>>> nonEmptySetIter = testSet.nonEmptySetIterator(); nonEmptySetIter.hasNext(); ) {
            HashSet<HashSet<TreeEdge>> perNeuronIndex = nonEmptySetIter.next();
            for (HashSet<TreeEdge> set : perNeuronIndex) {
                m = m + 1;
                if (set.size() > 1) {
                    System.out.println("Kanten in Mehrfach-Set " + n + ".:");
                    m = m + 1;
                    n = n + 1;
                    for (TreeEdge edge : set) System.out.println("Kante " + edge.toString());
                }
            }
        }
        System.out.println("Anzahl Sets aller Größe: " + m);
        System.out.println(" ");
    }

    public static void equTupelTest(HashSet<EqualityTupel> testSet) {
        System.out.println("Anzahl der EquTupel " + testSet.size());
        for (EqualityTupel tupel : testSet) equTupelStringOut(tupel);
    }

    public static void plausiblTestLoopPrevent(HashSet<EqualityTupel> testSet){
        System.out.println("Anzahl der EquTupel " + testSet.size());
    }

    public static void nonConflictSetsTest(HashSet<HashSet<EqualityTupel>> nonConflictSets) {
        System.out.println("Anzahl Konfliktfreier Equ-Tupel-Mengen: " + nonConflictSets.size());
        int n = 0;
        for (Iterator<HashSet<EqualityTupel>> nCIter = nonConflictSets.iterator(); nCIter.hasNext();){
            System.out.println("Anzahl Equ-Tupel in Menge " + (n+1) + ": " + nCIter.next().size());
            n = n + 1;
        }
    }
    public static int subTreeTest(TreeNode rootNode, int cnt){
        int next = cnt+1;
        TreeNode actNode = rootNode;
        if (actNode.hasSuccEdges()){
            System.out.print("Pfad-Kn-Nr." + cnt + ": ");
            for (TreeEdge e: actNode.getSucEdges()){
                System.out.print(e.toString() + " <" + e.getNumOfPaths() + "> | ");
            }
            System.out.println(" ");
            for (TreeEdge e: actNode.getSucEdges()) subTreeTest(e.getSuccessorNodeInTree(), next);
        }
        else System.out.println("--EoP--");
        return next;
    }
    public static void chooseBestnonConflictSetTest(HashSet<HashSet<EqualityTupel>> nonConflictSets){
        HashSet<EqualityTupel> nCS = nonConflictSets.iterator().next();
        System.out.println("Anzahl Tupel in Set " + nCS.size());
        TestContainer.singleNonConfliktSetStringOut(nCS);
    }

    public static void equTupelStringOut(EqualityTupel tupel) {
        System.out.println("Tupel für Equivalenzkante " + tupel.getRevEdge().toString());
        int n = 0;
        for (TreePath path : tupel.getConnPaths()) {
            System.out.print("Pfad Nr, " + n + " [" + path.getEdges().length + " Kanten] | ");
            n = n + 1;
        }
        System.out.println(" ");
    }

    public static void singleNonConfliktSetStringOut(HashSet<EqualityTupel> nonConflictSet){
        System.out.println("Konfliktfreie Menge mit: ");
        for (EqualityTupel tupel: nonConflictSet) equTupelStringOut(tupel);
    }
    public static void nonConfliktSetsStringOut(HashSet<HashSet<EqualityTupel>> nonConflictSets){
        int n = 1;
        for (HashSet<EqualityTupel> nCS: nonConflictSets){
            System.out.print(n + ".te ");
            singleNonConfliktSetStringOut(nCS);
            n = n + 1;
        }
    }

}
