package ru.fcpsr.domainsport.enums;

public enum Role {
    ADMIN("Админ"),
    USER("Пользователь"),
    MANAGER("Менеджер");

    private String title;
    Role(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
