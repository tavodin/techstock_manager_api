package io.github.tavodin.techstock_manager.dto;

public class MenuProjection {
    private Long menuId;
    private String menu;
    private String menuIcon;

    private Long menuItemId;
    private String name;
    private String link;
    private String menuItemIcon;

    public MenuProjection(
            Long menuId,
            String menu,
            String menuIcon,
            Long menuItemId,
            String name,
            String link,
            String menuItemIcon) {

        this.menuId = menuId;
        this.menu = menu;
        this.menuIcon = menuIcon;
        this.menuItemId = menuItemId;
        this.name = name;
        this.link = link;
        this.menuItemIcon = menuItemIcon;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getMenuIcon() {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
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

    public String getMenuItemIcon() {
        return menuItemIcon;
    }

    public void setMenuItemIcon(String menuItemIcon) {
        this.menuItemIcon = menuItemIcon;
    }
}
