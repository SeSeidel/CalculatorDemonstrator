package Calculator.general;

import Calculator.element_types.LayerType;
import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.NeuralNet;
import Calculator.tree_elements.TreeEdge;
import Calculator.tree_elements.TreePath;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class EquEdgesSet {

    private ArrayList<HashSet<HashSet<TreeEdge>>> data = new ArrayList<>();
    private NeuralNet net;
    private int filterLength;

    public EquEdgesSet(HashSet<TreePath> allPathsfromDecLvl, NeuralNet net){
        this.net = net;
        Pair<Integer, Integer> filterLAndArrLength = calcFilLengthAndDataStrucLengthForDecLvl(allPathsfromDecLvl.iterator().next().getDecLevelNum());
        filterLength = filterLAndArrLength.getKey();
        ArrayList<HashSet<TreeEdge>> indexBuckets = new ArrayList<>();
        for (int i = 0; i < filterLAndArrLength.getValue(); i++){                   // Initialisierung der Speicherstruktur mit der Länge der addierten Schichtfilter
            data.add(new HashSet<>());
            indexBuckets.add(new HashSet<>());
        }
        for (TreePath path: allPathsfromDecLvl){
            for (TreeEdge edge: path.getEdges()){                                   // Entspricht Kriterium der Ableitung vom gleichen Neuron!
                HashSet<TreeEdge> targetBucket = indexBuckets.get(edge.getSourceNeuronNum() + edge.getSourceNeuronFilterNum()*filterLAndArrLength.getKey());
                targetBucket.add(edge);
            }
        }
        for (int i = 0; i < indexBuckets.size(); i++){
            HashSet<TreeEdge> actSet = indexBuckets.get(i);
            if (actSet.size() > 1){                                                 // Körbe für Neuronen ohne zugehörige Kanten werden nicht verarbeitet.
                data.get(i).addAll(splitBucket(actSet));
            }
            else if (!actSet.isEmpty())data.get(i).add(actSet);
        }
    }
    /**
     * Spaltet eine Menge von Kanten zu einem abgeleiteten Neuron in Mengen gleichwertiger Kanten auf und gibt die Menge dieser Mengen zurück.
     */
    private HashSet<HashSet<TreeEdge>> splitBucket(HashSet<TreeEdge> edgeSet){
        HashSet<HashSet<TreeEdge>> out = new HashSet<>();
        while (!edgeSet.isEmpty()){                                                  // Solange noch Kanten ohne zugehörige Gleichwertigkeitsmenge in der Eingabemenge sind.
            Iterator<TreeEdge> edgeIter = edgeSet.iterator();
            HashSet<TreeEdge> compSet = new HashSet<>();
            TreeEdge compEdge = edgeIter.next(); compSet.add(compEdge);             // Nächste Kante in eigene Gleichwertigkeitsmenge überführen.
            while (edgeIter.hasNext()){                                              // Prüfen, ob von den restlichen Kanten weitere in diese Menge gehören.
                TreeEdge nextEdge = edgeIter.next();
                if (compEdge.equalEdge(nextEdge)) compSet.add(nextEdge);
            }
            out.add(compSet); edgeSet.removeAll(compSet);                           // Neue Gleichwertigkeitsmenge in Ausgabe übernehmen und aus Eingabemenge löschen.
        }
        return out;
    }   // Ende Punkt 2.

    /**
     * Berechnet die Länge der Filter in denen die Neuronen liegen, von denen die Kanten der Pfade mit decLvl abgeleitet wurden, sowie das Produkt dieser Länge mal der Filteranzahl.
     * @param decLvl Die Entscheidungsebene der Pfade, welche die zu sortierenden Kanten enthalten
     * @return Ein Tupel = <gesuchte Filterlänge, gesuchte Filterlänge * zugehörige Filterzahl>
     */
    private Pair<Integer, Integer> calcFilLengthAndDataStrucLengthForDecLvl(int decLvl){
        decLvl = decLvl + 1;                                                                                          // Entscheidungsebene + 1 wählen um passende Vorgängerschicht für Struktur zu wählen.
        NeuralLayer corrLayer = net.getNeuralLayers().get(net.getNeuralLayers().size()-decLvl);
        if (corrLayer.getLayerType().equals(LayerType.CORE_FLATTEN)) corrLayer = net.getNeuralLayers().get(net.getNeuralLayers().size()-decLvl-1);    // Bei Flatten nochmal decLvl +1, da hier ein Sprung stattfindet.
        int filterLength = corrLayer.getFilterNeurons(0).length;                                                // Geht, wie üblich, von gleicher Filterlänge je Schicht aus.
        int arrlength = corrLayer.getNumOfFilters() * filterLength;
        return new Pair<>(filterLength, arrlength);
    }

    /**
     * Gibt dem zum Ursprungsneuron der jeweiligen Kante gehörenden Sat an Mengen gleichwertiger Kanten aus.
     * @param neuronNum Indexnummer des zur Kante gehörenden Neurons.
     * @param neuronFilterNum Index des Filter, in dem das zur Kante gehörende Neuron liegt.
     * @return
     */
    public HashSet<HashSet<TreeEdge>> getNeuronRelatedEdges(int neuronNum, int neuronFilterNum){
        return data.get(neuronNum + neuronFilterNum*filterLength);
    }

    public Iterator<HashSet<HashSet<TreeEdge>>> nonEmptySetIterator(){
        ArrayList<HashSet<HashSet<TreeEdge>>> iterSource = new ArrayList<>();
        for (HashSet<HashSet<TreeEdge>> set: data) if (!set.isEmpty()) iterSource.add(set);
        return iterSource.iterator();
    }
    public int getNumOfNeuronDataSets(){ return data.size(); }
}
