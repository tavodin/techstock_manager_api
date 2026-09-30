package io.github.tavodin.techstock_manager.dto;

import java.util.*;

public class UserMenuDTO {

    private Long id;
    private String menu;
    private String icon;
    private List<MenuItemDTO> menuItem = new ArrayList<>();

    public UserMenuDTO() {
    }

    public UserMenuDTO(Long id, String menu, String icon, List<MenuItemDTO> menuItem) {
        this.id = id;
        this.menu = menu;
        this.icon = icon;
        this.menuItem = menuItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<MenuItemDTO> getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(List<MenuItemDTO> menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserMenuDTO that = (UserMenuDTO) o;
        return Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(menu);
    }
}
