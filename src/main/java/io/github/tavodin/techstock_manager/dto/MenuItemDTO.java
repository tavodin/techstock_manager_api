package io.github.tavodin.techstock_manager.dto;

public class MenuItemDTO {
    private Long id;
    private String name;
    private String link;
    private String icon;

    public MenuItemDTO() {
    }

    public MenuItemDTO(Long id, String name, String link, String icon) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.icon = icon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
