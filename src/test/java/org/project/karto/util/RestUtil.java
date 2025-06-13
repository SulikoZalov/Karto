package org.project.karto.util;

import jakarta.ws.rs.WebApplicationException;
import org.json.JSONObject;

public class RestUtil {

    public static String errorMessage(WebApplicationException e) {
        JSONObject jsonObject = new JSONObject(e.getResponse().getEntity().toString());
        return jsonObject.getString("errorMessage");
    }
}
