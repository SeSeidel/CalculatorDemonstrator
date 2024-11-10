package Calculator.tree_elements;

public class TreeDecision {

    private double[] decInput;
    private TreeEdge outerEdge;
    private double[] decOutput;

    public TreeDecision(double[] input, TreeEdge outerEdge, double[] output){
        this.decInput = input;
        this.outerEdge = outerEdge;
        this.decOutput = output;
    }

    public double[] getDecInput() { return decInput; }
    public TreeEdge getOuterEdge() { return outerEdge; }
    public double[] getDecOutput() { return decOutput; }

    @Override
    public boolean equals(Object o) {       //Für saubere Datanstruktur ggf. noch layerNum einarbeiten - Sollte jedoch so gut wie nie nötig sein wenn die Gewichte UND der Bias abgefragt werden! (Lediglich für Pooling, daher Filternummer)
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeDecision treeDec = (TreeDecision) o;
        return decInput.equals(treeDec.getDecInput());
    }
}
