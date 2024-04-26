package ru.fcpsr.domainsport.enums;

public enum Access {
    NEWS("Новости"),
    EVENT("Спортмероприятия"),
    SCHOOL("Школы"),
    OBJECT("Объекты"),
    POST("Статьи");

    private final String title;
    Access(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
