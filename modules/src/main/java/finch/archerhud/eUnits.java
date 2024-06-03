package finch.archerhud;



public enum eUnits {
    None(0),
    Metres(1),
    Kilometres(3),
    Miles(5),
    Foot(8);

    final public int value;

    eUnits(int value) {
        this.value = value;
    }
}
