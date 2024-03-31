package ru.fcpsr.domainsport.enums;

public enum Permission {
    CREATE("Создание"),
    READ("Чтение"),
    UPDATE("Обновление"),
    DELETE("Удаление");
    private final String title;

    Permission(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
