package io.github.tavodin.techstock_manager.dto;

public class MenuItemDTO {
    private String name;
    private String link;

    public MenuItemDTO() {
    }

    public MenuItemDTO(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
