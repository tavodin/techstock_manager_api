package io.github.tavodin.techstock_manager.dto;

import java.util.*;

public class UserMenuDTO {

    private String menu;
    private List<MenuItemDTO> menuItem = new ArrayList<>();

    public UserMenuDTO() {
    }

    public UserMenuDTO(String menu, List<MenuItemDTO> menuItem) {
        this.menu = menu;
        this.menuItem = menuItem;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
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
