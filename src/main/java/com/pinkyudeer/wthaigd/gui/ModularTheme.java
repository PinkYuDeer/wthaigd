package com.pinkyudeer.wthaigd.gui;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.utils.JsonBuilder;
import com.google.gson.Gson;

public class ModularTheme {

    public static JsonBuilder myTheme = new JsonBuilder();

    public static void init() {
        String ThemeJson = """
            {
              "parent": "DEFAULT",
              "background": null,
              "hoverBackground": "none",
              "color": "#FFFFFFFF",
              "textColor": "#FF404040",
              "textShadow": false,
              "panel": {
                "background": null,
                "hoverBackground": "none",
                "color": "#00000055"
              },
              "button": {
                "background": {
                  "type": "texture",
                  "id": "mc_button"
                },
                "hoverBackground": {
                  "type": "texture",
                  "id": "mc_button_hovered"
                },
                "textColor": "#FFFFFFFF",
                "textShadow": true
              },
              "itemSlot": {
                "background": {
                  "type": "texture",
                  "id": "slot_item"
                },
                "slotHoverColor": "#60FFFFFF"
              },
              "fluidSlot": {
                "background": {
                  "type": "texture",
                  "id": "slot_fluid"
                },
                "slotHoverColor": "#60FFFFFF"
              },
              "textField": {
                "background": {
                  "type": "texture",
                  "id": "display_small"
                },
                "textColor": "#FFFFFFFF",
                "markedColor": "#FF2F72A8",
                "hintColor": "#FF5F5F5F"
              },
              "toggleButton": {
                "background": {
                  "type": "texture",
                  "id": "mc_button"
                },
                "hoverBackground": {
                  "type": "texture",
                  "id": "mc_button_hovered"
                },
                "textColor": "#FFFFFFFF",
                "textShadow": true,
                "selectedBackground": {
                  "type": "texture",
                  "id": "mc_button_disabled"
                },
                "selectedHoverBackground": "none",
                "selectedColor": "#FFFFFFFF",
                "selectedTextColor": "#FFFFFFFF",
                "selectedTextShadow": true
              }
            }
            """;
        Gson gson = new Gson();
        myTheme = gson.fromJson(ThemeJson, JsonBuilder.class);
        IThemeApi.get()
            .registerTheme("wthaigd:main", myTheme);
    }
}
