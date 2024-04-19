package ru.fcpsr.domainsport.enums;

public enum Role {
    ADMIN("Админ"),
    MODERATOR("Модератор"),
    MANAGER("Менеджер"),
    WORKER("Сотрудник"),
    USER("Пользователь");

    private String title;
    Role(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
