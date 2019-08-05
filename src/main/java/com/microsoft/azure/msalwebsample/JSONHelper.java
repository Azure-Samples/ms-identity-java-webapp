// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.msalwebsample;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helpers for dealing with JSON formatted data
 */
class JSONHelper {

    static JSONArray fetchDirectoryObjectJSONArray(JSONObject jsonObject) {
        JSONArray jsonArray;
        jsonArray = jsonObject.optJSONObject("responseMsg").optJSONArray("value");
        return jsonArray;
    }

    static <T> void convertJSONObjectToDirectoryObject(JSONObject jsonObject, T destObject)
            throws Exception {

        Field[] fieldList = destObject.getClass().getDeclaredFields();

        // For all the declared field.
        for (int i = 0; i < fieldList.length; i++) {
            // If the field is of type String, that is
            // if it is a simple attribute.
            if (fieldList[i].getType().equals(String.class)) {
                // Invoke the corresponding set method of the destObject using
                // the argument taken from the jsonObject.
                destObject.getClass().getMethod(
                        String.format("set%s", StringUtils.capitalize(fieldList[i].getName())),
                        new Class[] { String.class }).invoke(
                                destObject,
                        new Object[] { jsonObject.optString(fieldList[i].getName()) });
            }
        }
    }
}
