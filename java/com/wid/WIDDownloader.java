package com.wid;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.json.*;
import com.stata.sfi.*;

public class WIDDownloader {

    private static String apiKey = "<private API key>";

    private static String apiCountriesAvailableVariables = "https://rfap9nitz6.execute-api.eu-west-1.amazonaws.com/prod/countries-available-variables";
    private static String apiCountriesVariables          = "https://rfap9nitz6.execute-api.eu-west-1.amazonaws.com/prod/countries-variables";
    private static String apiCountriesVariablesMetadata  = "https://rfap9nitz6.execute-api.eu-west-1.amazonaws.com/prod/countries-variables-metadata";

    public static int importCountriesAvailableVariables(String[] args) {
        // Retrieve the arguments of the query
        String countries = args[0];
        String sixlet = args[1];

        // Create the query
        String query;
        try {
            String charset = java.nio.charset.StandardCharsets.UTF_8.name();
            query = String.format("countries=%s&variables=%s",
                URLEncoder.encode(countries, charset),
                URLEncoder.encode(sixlet, charset)
            );
        } catch (Exception e) {
            SFIToolkit.error("\nthe 'areas' argument contains invalid characters\n");
            return(198);
        }

        // Access the online database
        Scanner scanner;
        String response;
        try {
            // Perform the GET query
            URL queryURL = new URL(apiCountriesAvailableVariables + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) queryURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", apiKey);

            // Read the response
            scanner = new Scanner(connection.getInputStream());
            response = new String();
            while (scanner.hasNext()) {
                response += scanner.nextLine();
            }
            scanner.close();

        } catch (Exception e) {
            SFIToolkit.error("\ncould not access the online WID.world database; please check your internet connection\n");
            return(677);
        }

        // Parse the results
        try {
            List<String> listVariable   = new ArrayList<String>();
            List<String> listCountry    = new ArrayList<String>();
            List<String> listPercentile = new ArrayList<String>();
            List<String> listAge        = new ArrayList<String>();
            List<String> listPop        = new ArrayList<String>();

            // Correction for cases where an array is returned
            JSONObject json;
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                JSONArray jsonArray = new JSONArray(response);
                json = jsonArray.getJSONObject(0);
            }

            Iterator<String> variableIter = json.keys();
            while (variableIter.hasNext()) {
                String variable = variableIter.next();
                JSONObject jsonCountry = json.getJSONObject(variable);
                Iterator<String> countryIter = jsonCountry.keys();
                while (countryIter.hasNext()) {
                    String country = countryIter.next();
                    JSONArray jsonProperties = jsonCountry.getJSONArray(country);
                    Iterator propertiesIter = jsonProperties.iterator();
                    while (propertiesIter.hasNext()) {
                        JSONArray properties = (JSONArray) propertiesIter.next();

                        listVariable.add(variable);
                        listCountry.add(country);
                        listPercentile.add(properties.optString(0));
                        listAge.add(properties.optString(1));
                        listPop.add(properties.optString(2));
                    }
                }
            }

            // Fill the Stata dataset
            Data.addVarStr("variable", 6);
            Data.addVarStr("country", 5);
            Data.addVarStr("percentile", 14);
            Data.addVarStr("age", 3);
            Data.addVarStr("pop", 1);

            int obsCount = listVariable.size();
            Data.setObsCount(obsCount);

            int variableVariableIndex   = Data.getVarIndex("variable");
            int variableCountryIndex    = Data.getVarIndex("country");
            int variablePercentileIndex = Data.getVarIndex("percentile");
            int variableAgeIndex        = Data.getVarIndex("age");
            int variablePopIndex        = Data.getVarIndex("pop");

            for (int i = 0; i < obsCount; i++) {
                Data.storeStr(variableVariableIndex,   i + 1, listVariable.get(i));
                Data.storeStr(variableCountryIndex,    i + 1, listCountry.get(i));
                Data.storeStr(variablePercentileIndex, i + 1, listPercentile.get(i));
                Data.storeStr(variableAgeIndex,        i + 1, listAge.get(i));
                Data.storeStr(variablePopIndex,        i + 1, listPop.get(i));
            }
        } catch (Exception e) {
            SFIToolkit.error("\nserver response invalid; if the problem persists, please file bug report to thomas.blanchet@wid.com\n");
            SFIToolkit.error(e.toString());
            SFIToolkit.error("\n");
            return(674);
        }

        return(0);
    }

    public static int importCountriesVariables(String[] args) {
        // Retrieve the arguments of the query
        String countries = args[0];
        String variables = args[1];

        // Create the query
        String query;
        try {
            String charset = java.nio.charset.StandardCharsets.UTF_8.name();
            query = String.format("countries=%s&variables=%s",
                URLEncoder.encode(countries, charset),
                URLEncoder.encode(variables, charset)
            );
        } catch (Exception e) {
            SFIToolkit.error("\nthe arguments contains invalid characters\n");
            return(198);
        }

        // Access the online database
        Scanner scanner;
        String response;
        try {
            // Perform the GET query
            URL queryURL = new URL(apiCountriesVariables + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) queryURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", apiKey);

            // Read the response
            scanner = new Scanner(connection.getInputStream());
            response = new String();
            while (scanner.hasNext()) {
                response += scanner.nextLine();
            }
            scanner.close();
        } catch (Exception e) {
            SFIToolkit.error("\ncould not access the online WID.world database; please check your internet connection\n");
            return(677);
        }

        // Parse the results
        try {
            List<String>  listCountry    = new ArrayList<String>();
            List<String>  listVariable   = new ArrayList<String>();
            List<String>  listPercentile = new ArrayList<String>();
            List<String>  listAge        = new ArrayList<String>();
            List<String>  listPop        = new ArrayList<String>();
            List<Integer> listYear       = new ArrayList<Integer>();
            List<Double>  listValue      = new ArrayList<Double>();

            JSONObject json = new JSONObject(response);

            Iterator<String> indicatorIter = json.keys();
            while (indicatorIter.hasNext()) {
                String indicator = indicatorIter.next();
                JSONArray indicatorData = json.getJSONArray(indicator);
                Iterator indicatorDataIter = indicatorData.iterator();
                while (indicatorDataIter.hasNext()) {
                    JSONObject countryData = (JSONObject) indicatorDataIter.next();

                    String country = JSONObject.getNames(countryData)[0];
                    JSONArray countryValues = countryData.getJSONObject(country).getJSONArray("values");

                    Iterator countryValuesIter = countryValues.iterator();
                    while (countryValuesIter.hasNext()) {
                        JSONObject value = (JSONObject) countryValuesIter.next();

                        String[] parts = indicator.split("_");

                        listCountry.add(country);
                        listVariable.add(parts[0]);
                        listPercentile.add(parts[1]);
                        listAge.add(parts[2]);
                        listPop.add(parts[3]);
                        listYear.add(value.getInt("y"));
                        listValue.add(value.getDouble("v"));
                    }
                }
            }

            // Fill the Stata dataset
            Data.addVarStr("variable", 6);
            Data.addVarStr("country", 5);
            Data.addVarStr("percentile", 14);
            Data.addVarStr("age", 3);
            Data.addVarStr("pop", 1);
            Data.addVarInt("year");
            Data.addVarDouble("value");

            int obsCount = listVariable.size();
            Data.setObsCount(obsCount);

            int variableCountryIndex    = Data.getVarIndex("country");
            int variableVariableIndex   = Data.getVarIndex("variable");
            int variablePercentileIndex = Data.getVarIndex("percentile");
            int variableAgeIndex        = Data.getVarIndex("age");
            int variablePopIndex        = Data.getVarIndex("pop");
            int variableYearIndex       = Data.getVarIndex("year");
            int variableValueIndex      = Data.getVarIndex("value");

            for (int i = 0; i < obsCount; i++) {
                Data.storeStr(variableCountryIndex,    i + 1, listCountry.get(i));
                Data.storeStr(variableVariableIndex,   i + 1, listVariable.get(i));
                Data.storeStr(variablePercentileIndex, i + 1, listPercentile.get(i));
                Data.storeStr(variableAgeIndex,        i + 1, listAge.get(i));
                Data.storeStr(variablePopIndex,        i + 1, listPop.get(i));
                Data.storeNum(variableYearIndex,       i + 1, listYear.get(i));
                Data.storeNum(variableValueIndex,      i + 1, listValue.get(i));
            }
        } catch (Exception e) {
            SFIToolkit.error("\nserver response invalid; if the problem persists, please file bug report to thomas.blanchet@wid.com\n");
            SFIToolkit.error(e.toString());
            SFIToolkit.error("\n");
            return(674);
        }

        return(0);
    }

    public static int importCountriesVariablesMetadata(String[] args) {
        // Retrieve the arguments of the query
        String countries = args[0];
        String variables = args[1];

        // Create the query
        String query;
        try {
            String charset = java.nio.charset.StandardCharsets.UTF_8.name();
            query = String.format("countries=%s&variables=%s",
                URLEncoder.encode(countries, charset),
                URLEncoder.encode(variables, charset)
            );
        } catch (Exception e) {
            SFIToolkit.error("\nthe 'areas' argument contains invalid characters\n");
            return(198);
        }

        // Access the online database
        Scanner scanner;
        String response;
        try {
            // Perform the GET query
            URL queryURL = new URL(apiCountriesVariablesMetadata + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) queryURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", apiKey);

            // Read the response
            scanner = new Scanner(connection.getInputStream());
            response = new String();
            while (scanner.hasNext()) {
                response += scanner.nextLine();
            }
            scanner.close();
        } catch (Exception e) {
            SFIToolkit.error("\ncould not access the online WID.world database; please check your internet connection\n");
            SFIToolkit.error(e.toString());
            SFIToolkit.error("\n");
            return(677);
        }

        // Parse the results
        try {
            List<String> listVariable    = new ArrayList<String>();
            List<String> listCountry     = new ArrayList<String>();
            List<String> listCountryName = new ArrayList<String>();
            List<String> listPercentile  = new ArrayList<String>();
            List<String> listPop         = new ArrayList<String>();
            List<String> listAge         = new ArrayList<String>();
            int sizeCountryName = 0;

            List<String> listShortName    = new ArrayList<String>();
            List<String> listSimpleDes    = new ArrayList<String>();
            List<String> listTechnicalDes = new ArrayList<String>();
            int sizeShortName = 0;
            int sizeSimpleDes = 0;
            int sizeTechnicalDes = 0;

            List<String> listShortType = new ArrayList<String>();
            List<String> listLongType  = new ArrayList<String>();
            int sizeShortType = 0;
            int sizeLongType = 0;

            List<String> listShortPop = new ArrayList<String>();
            List<String> listLongPop  = new ArrayList<String>();
            int sizeShortPop = 0;
            int sizeLongPop = 0;

            List<String> listShortAge = new ArrayList<String>();
            List<String> listLongAge  = new ArrayList<String>();
            int sizeShortAge = 0;
            int sizeLongAge = 0;

            List<String> listUnit      = new ArrayList<String>();
            List<String> listUnitLabel = new ArrayList<String>();
            int sizeUnit = 0;
            int sizeUnitLabel = 0;

            List<String> listSource = new ArrayList<String>();
            List<String> listMethod = new ArrayList<String>();
            int sizeSource = 0;
            int sizeMethod = 0;

            JSONArray json = new JSONArray(response).getJSONObject(0).getJSONArray("metadata_func");

            // First, loop over the indicator
            Iterator indicatorIter = json.iterator();
            while (indicatorIter.hasNext()) {
                JSONObject indicator = (JSONObject) indicatorIter.next();
                String indicatorName = JSONObject.getNames(indicator)[0];
                JSONArray indicatorData = indicator.getJSONArray(indicatorName);

                String[] parts = indicatorName.split("_");
                String variable = parts[0];
                String percentile = parts[1];
                String age = parts[2];
                String pop = parts[3];
                String concept = variable.substring(2, 6);

                JSONObject nameJSON = indicatorData.getJSONObject(0).getJSONObject("name");
                JSONObject typeJSON = indicatorData.getJSONObject(1).getJSONObject("type");
                JSONObject popJSON  = indicatorData.getJSONObject(2).getJSONObject("pop");
                JSONObject ageJSON  = indicatorData.getJSONObject(3).getJSONObject("age");
                JSONArray unitJSON  = indicatorData.getJSONObject(4).getJSONArray("units");
                JSONObject notesJSON = indicatorData.getJSONObject(5).getJSONArray("notes").getJSONObject(0);

                String shortName = nameJSON.optString("shortname");
                String simpleDes = nameJSON.optString("simpledes");
                String technicalDes = nameJSON.optString("technicaldes");

                String shortType = typeJSON.optString("shortdes");
                String longType = typeJSON.optString("longdes");

                String shortPop = popJSON.optString("shortdes");
                String longPop = popJSON.optString("longdes");

                String shortAge = ageJSON.optString("shortname");
                String longAge = ageJSON.optString("fullname");

                String country;
                String countryName;

                String unit;
                String unitLabel;

                String source = "";
                String method = "";

                // The item "unit" (5th position) is always filled, so we use it to
                // loop over the different countries
                Iterator countryIter = unitJSON.iterator();
                while (countryIter.hasNext()) {
                    JSONObject countryUnits = (JSONObject) countryIter.next();

                    country = countryUnits.optString("country");
                    countryName = countryUnits.optString("country_name");
                    unit = countryUnits.getJSONObject("metadata").optString("unit");
                    unitLabel = countryUnits.getJSONObject("metadata").optString("unit_name");

                    // Find matching source and method, if any
                    if (!notesJSON.isNull(concept)) {
                        Iterator countryNotesIter = notesJSON.getJSONArray(concept).iterator();
                        while (countryNotesIter.hasNext()) {
                            JSONObject countryNotes = (JSONObject) countryNotesIter.next();
                            if (countryNotes.optString("alpha2").equals(country)) {
                                source = countryNotes.optString("source");
                                method = countryNotes.optString("method");
                                break;
                            }
                        }
                    }

                    listVariable.add(variable);
                    listPercentile.add(percentile);
                    listCountry.add(country);
                    listAge.add(age);
                    listPop.add(pop);
                    listCountryName.add(countryName);
                    if (countryName.length() > sizeCountryName) {
                        sizeCountryName = countryName.length();
                    }

                    listShortName.add(shortName);
                    if (shortName.length() > sizeShortName) {
                        sizeShortName = shortName.length();
                    }
                    listSimpleDes.add(simpleDes);
                    if (simpleDes.length() > sizeSimpleDes) {
                        sizeSimpleDes = simpleDes.length();
                    }
                    listTechnicalDes.add(technicalDes);
                    if (technicalDes.length() > sizeTechnicalDes) {
                        sizeTechnicalDes = technicalDes.length();
                    }

                    listShortType.add(shortType);
                    if (shortType.length() > sizeShortType) {
                        sizeShortType = shortType.length();
                    }
                    listLongType.add(longType);
                    if (longType.length() > sizeLongType) {
                        sizeLongType = longType.length();
                    }

                    listShortPop.add(shortPop);
                    if (shortPop.length() > sizeShortPop) {
                        sizeShortPop = shortPop.length();
                    }
                    listLongPop.add(longPop);
                    if (longPop.length() > sizeLongPop) {
                        sizeLongPop = longPop.length();
                    }

                    listShortAge.add(shortAge);
                    if (shortAge.length() > sizeShortAge) {
                        sizeShortAge = shortAge.length();
                    }
                    listLongAge.add(longAge);
                    if (longAge.length() > sizeLongAge) {
                        sizeLongAge = longAge.length();
                    }

                    listUnit.add(unit);
                    if (unit.length() > sizeUnit) {
                        sizeUnit = unit.length();
                    }
                    listUnitLabel.add(unitLabel);
                    if (unitLabel.length() > sizeUnitLabel) {
                        sizeUnitLabel = unitLabel.length();
                    }

                    listSource.add(source);
                    if (source.length() > sizeSource) {
                        sizeSource = source.length();
                    }
                    listMethod.add(method);
                    if (method.length() > sizeMethod) {
                        sizeMethod = method.length();
                    }
                }
            }

            // Fill the Stata dataset
            Data.addVarStr("variable", 6);
            Data.addVarStr("percentile", 14);
            Data.addVarStr("country", 5);
            Data.addVarStr("countryname", sizeCountryName);
            Data.addVarStr("age", 3);
            Data.addVarStr("pop", 1);

            Data.addVarStr("shortname", sizeShortName);
            Data.addVarStr("simpledes", sizeSimpleDes);
            Data.addVarStr("technicaldes", sizeTechnicalDes);

            Data.addVarStr("shorttype", sizeShortType);
            Data.addVarStr("longtype", sizeLongType);

            Data.addVarStr("shortpop", sizeShortPop);
            Data.addVarStr("longpop", sizeLongPop);

            Data.addVarStr("shortage", sizeShortAge);
            Data.addVarStr("longage", sizeLongAge);

            Data.addVarStr("unit", sizeUnit);
            Data.addVarStr("unitlabel", sizeUnitLabel);

            Data.addVarStr("source", sizeSource);
            Data.addVarStr("method", sizeMethod);

            int obsCount = listVariable.size();
            Data.setObsCount(obsCount);

            int variableVariableIndex    = Data.getVarIndex("variable");
            int variablePercentileIndex  = Data.getVarIndex("percentile");
            int variableCountryIndex     = Data.getVarIndex("country");
            int variableCountryNameIndex = Data.getVarIndex("countryname");
            int variableAgeIndex         = Data.getVarIndex("age");
            int variablePopIndex         = Data.getVarIndex("pop");

            int variableShortNameIndex    = Data.getVarIndex("shortname");
            int variableSimpleDesIndex    = Data.getVarIndex("simpledes");
            int variableTechnicalDesIndex = Data.getVarIndex("technicaldes");

            int variableShortTypeIndex = Data.getVarIndex("shorttype");
            int variableLongTypeIndex  = Data.getVarIndex("longtype");

            int variableShortPopIndex = Data.getVarIndex("shortpop");
            int variableLongPopIndex  = Data.getVarIndex("longpop");

            int variableShortAgeIndex = Data.getVarIndex("shortage");
            int variableLongAgeIndex  = Data.getVarIndex("longage");

            int variableUnitIndex      = Data.getVarIndex("unit");
            int variableUnitLabelIndex = Data.getVarIndex("unitlabel");

            int variableSourceIndex = Data.getVarIndex("source");
            int variableMethodIndex = Data.getVarIndex("method");

            for (int i = 0; i < obsCount; i++) {
                Data.storeStr(variableVariableIndex,    i + 1, listVariable.get(i));
                Data.storeStr(variablePercentileIndex,  i + 1, listPercentile.get(i));
                Data.storeStr(variableCountryIndex,     i + 1, listCountry.get(i));
                Data.storeStr(variableCountryNameIndex, i + 1, listCountryName.get(i));
                Data.storeStr(variableAgeIndex,         i + 1, listAge.get(i));
                Data.storeStr(variablePopIndex,         i + 1, listPop.get(i));

                Data.storeStr(variableShortNameIndex,    i + 1, listShortName.get(i));
                Data.storeStr(variableSimpleDesIndex,    i + 1, listSimpleDes.get(i));
                Data.storeStr(variableTechnicalDesIndex, i + 1, listTechnicalDes.get(i));

                Data.storeStr(variableShortTypeIndex, i + 1, listShortType.get(i));
                Data.storeStr(variableLongTypeIndex,  i + 1, listLongType.get(i));

                Data.storeStr(variableShortPopIndex, i + 1, listShortPop.get(i));
                Data.storeStr(variableLongPopIndex,  i + 1, listLongPop.get(i));

                Data.storeStr(variableShortAgeIndex, i + 1, listShortAge.get(i));
                Data.storeStr(variableLongAgeIndex,  i + 1, listLongAge.get(i));

                Data.storeStr(variableUnitIndex,      i + 1, listUnit.get(i));
                Data.storeStr(variableUnitLabelIndex, i + 1, listUnitLabel.get(i));

                Data.storeStr(variableSourceIndex, i + 1, listSource.get(i));
                Data.storeStr(variableMethodIndex, i + 1, listMethod.get(i));
            }
        } catch (Exception e) {
            SFIToolkit.error("\nserver response invalid; if the problem persists, please file bug report to thomas.blanchet@wid.com\n");
            SFIToolkit.error(e.toString());
            SFIToolkit.error("\n");
            return(674);
        }

        return(0);
    }

}
