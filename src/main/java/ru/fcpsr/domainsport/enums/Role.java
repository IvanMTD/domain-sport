package ru.fcpsr.domainsport.enums;

public enum Role {
    ADMIN("Админ"),
    MODERATOR("Модератор"),
    MANAGER("Менеджер"),
    WORKER("Сотрудник"),
    USER("Пользователь");

    private final String title;
    Role(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
