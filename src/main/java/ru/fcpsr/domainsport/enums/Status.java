package ru.fcpsr.domainsport.enums;

public enum Status {
    MS("Международные соревнования"),
    CR("Чемпионат России"),
    KR("Кубок России"),
    VS("Всероссийские соревнования"),
    MSS("Межрегиональные спортивные соревнования"),
    PR("Первенство России");

    private final String title;

    Status(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
