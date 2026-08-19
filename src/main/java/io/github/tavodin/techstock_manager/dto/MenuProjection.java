package io.github.tavodin.techstock_manager.dto;

public class MenuProjection {
    private String name;
    private String link;
    private String menu;

    public MenuProjection() {
    }

    public MenuProjection(String name, String link, String menu) {
        this.name = name;
        this.link = link;
        this.menu = menu;
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

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }
}
