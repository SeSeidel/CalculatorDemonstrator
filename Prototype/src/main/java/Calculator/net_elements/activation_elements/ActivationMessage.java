package Calculator.net_elements.activation_elements;

import java.util.HashSet;

public class ActivationMessage {

    private double activation;                  // Die übertragene Aktivierung.
    private HashSet<Integer> relInputFields;    // Die für die übetragene Aktivierung relevanten Eingangsverbindungen.
    private HashSet<Integer> conInputFields;    // Die mit dem Zielneuron verbundenen Eingangsverbindungen mit Gewicht != 0.

    public double getActivation() { return activation; }
    public HashSet<Integer> getRelInputsForActivation() { return relInputFields; }
    public HashSet<Integer> getConInputsForActivation() { return conInputFields; }

    public ActivationMessage (double activation, HashSet<Integer> relInputFields, HashSet<Integer> conInputFields){
        this.activation = activation;
        this.relInputFields = relInputFields;
        this.conInputFields = conInputFields;
    }
}
