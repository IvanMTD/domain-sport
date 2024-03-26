package ru.fcpsr.domainsport.enums;

public enum SportStatus {
    OLYMPIC("Олимпийский"),
    NO_OLYMPIC("Неолимпийский"),
    ADAPTIVE("Адаптивный");

    private final String title;

    SportStatus(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }
}
