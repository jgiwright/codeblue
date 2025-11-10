public abstract class Condition {
    protected String name;
    protected double timeToCardiacArrest;
    protected String[] treatmentsRequired;

    public Condition(String name, double timeToCardiacArrest, String[] treatmentsRequired) {
        this.name = name;
        this.timeToCardiacArrest = timeToCardiacArrest;
        this.treatmentsRequired = treatmentsRequired;
    }

    public String getName() { return name; }
    public double getTimeToCardiacArrest() { return timeToCardiacArrest; }
    public String[] getTreatmentsRequired() { return treatmentsRequired; }

}


class Anaphylaxis extends Condition {
    public Anaphylaxis() {
        super("Anaphylaxis", 10.0, new String[]{"adrenaline"});
    }
}