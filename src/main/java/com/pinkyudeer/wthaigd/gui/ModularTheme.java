package com.pinkyudeer.wthaigd.gui;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.utils.JsonBuilder;
import com.google.gson.Gson;

public class ModularTheme {

    public static void init() {
        String themeJson = """
            {
              "parent": "DEFAULT",
              "background": null,
              "hoverBackground": "none",
              "color": "#FFFFFFFF",
              "textColor": "#FFDDDDDD",
              "textShadow": true,
              "panel": {
                "background": null,
                "hoverBackground": "none",
                "color": "#00000000"
              },
              "button": {
                "background": null,
                "hoverBackground": "none",
                "textColor": "#FFFFFFFF",
                "textShadow": true
              },
              "textField": {
                "background": null,
                "textColor": "#FFFFFFFF",
                "markedColor": "#FF2F72A8",
                "hintColor": "#FF888888"
              },
              "toggleButton": {
                "background": null,
                "hoverBackground": "none",
                "textColor": "#FFFFFFFF",
                "textShadow": true,
                "selectedBackground": null,
                "selectedHoverBackground": "none",
                "selectedColor": "#FFFFFFFF",
                "selectedTextColor": "#FFFFFFFF",
                "selectedTextShadow": true
              }
            }
            """;
        JsonBuilder builder = new Gson().fromJson(themeJson, JsonBuilder.class);
        IThemeApi.get()
            .registerTheme("wthaigd:main", builder);
    }
}
