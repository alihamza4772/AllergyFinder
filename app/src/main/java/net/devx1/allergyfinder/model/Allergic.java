package net.devx1.allergyfinder.model;

public class Allergic {
    private String allergicTo;

    public Allergic(String allergicTo) {
        this.allergicTo = allergicTo;
    }

    public String getAllergicTo() {
        return allergicTo;
    }

    public void setAllergicTo(String allergicTo) {
        this.allergicTo = allergicTo;
    }
}
