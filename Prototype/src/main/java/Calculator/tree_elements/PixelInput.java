package Calculator.tree_elements;

import javafx.util.Pair;
import java.util.Objects;

public class PixelInput {
    private double value;
    private Pair<Integer, Integer> coords;

    public PixelInput(double val, Pair<Integer, Integer> coords){
       this.value = val;
       this.coords = coords;
    }

    public double getValue() { return value; }
    public int getxInd() { return coords.getKey(); }
    public int getyInd() { return coords.getValue(); }

    @Override
    public int hashCode() { return Objects.hash(value, coords); }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PixelInput pInp = (PixelInput) o;
        if (pInp.value == value && pInp.coords.getKey() == coords.getKey() && pInp.coords.getValue() == coords.getValue()) return true;
        return false;
    }
    @Override
    public String toString() { return ("val: " + value + ", xKoord: " + coords.getKey() + ", yKoord: " + coords.getValue()); }
}
