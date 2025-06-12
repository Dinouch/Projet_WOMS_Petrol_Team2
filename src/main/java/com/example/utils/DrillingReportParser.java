package com.example.utils;
import com.example.dao.FichierDrillingDAO;
import com.example.entities.FICHIER_DRILLING;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrillingReportParser {

    public static JSONObject parseDrillingReportHeader(Sheet sheet) {
        JSONObject headerInfo = new JSONObject();

        // Initialize with nulls to ensure these fields always exist in output
        headerInfo.put("report_number", JSONObject.NULL);
        headerInfo.put("well_name", JSONObject.NULL);
        headerInfo.put("report_date", JSONObject.NULL);
        headerInfo.put("depth_24h_ft", JSONObject.NULL);
        headerInfo.put("tvd_ft", JSONObject.NULL);
        headerInfo.put("progress_ft", JSONObject.NULL);
        headerInfo.put("progress_hours", JSONObject.NULL);
        headerInfo.put("last_casing", JSONObject.NULL);
        headerInfo.put("casing_at_ft", JSONObject.NULL);
        headerInfo.put("casing_top_at_ft", JSONObject.NULL);

        try {
            System.out.println("Starting extraction of well_name");

            // Extract well_name (columns AI to BA, rows 3 and 4)
            List<String> wellNameParts = new ArrayList<>();
            for (int rowNum : new int[]{2, 3}) { // Rows 3 and 4 (0-based)
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    for (int colNum = 34; colNum <= 52; colNum++) { // Columns AI to BA
                        Cell cell = row.getCell(colNum);
                        if (cell != null) {
                            String cellStr = getCellValueAsString(cell).trim();
                            if (!cellStr.isEmpty() && !cellStr.equalsIgnoreCase("nan")) {
                                wellNameParts.add(cellStr);
                            }
                        }
                    }
                }
            }

            String wellName = wellNameParts.isEmpty() ? null : String.join(" ", wellNameParts);
            if (wellName != null && !wellName.trim().isEmpty()) {
                headerInfo.put("well_name", wellName);
                System.out.println("DEBUG - well_name extracted: '" + wellName + "'");
            } else {
                // Try a fallback method to find well name elsewhere in the document
                wellName = findWellNameInSheet(sheet);
                if (wellName != null && !wellName.trim().isEmpty()) {
                    headerInfo.put("well_name", wellName);
                    System.out.println("DEBUG - well_name found by fallback: '" + wellName + "'");
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting well_name: " + e.getMessage());
            e.printStackTrace();
        }

        // Extract other fields from the full text
        String fullText = sheetToString(sheet);
        System.out.println("Full text extracted, length: " + fullText.length());

        // Report number and date
        try {
            Pattern numPattern = Pattern.compile("N°:\\s*(\\d+)");
            Pattern datePattern = Pattern.compile("Date:\\s*(\\d{2}/\\d{2}/\\d{4})");

            Matcher numMatcher = numPattern.matcher(fullText);
            Matcher dateMatcher = datePattern.matcher(fullText);

            if (numMatcher.find()) {
                headerInfo.put("report_number", numMatcher.group(1).trim());
                System.out.println("Found report number: " + numMatcher.group(1).trim());
            } else {
                // Try alternate pattern
                Pattern altNumPattern = Pattern.compile("N[°o]\\s*[:.=]\\s*(\\d+)");
                Matcher altNumMatcher = altNumPattern.matcher(fullText);
                if (altNumMatcher.find()) {
                    headerInfo.put("report_number", altNumMatcher.group(1).trim());
                    System.out.println("Found report number (alt): " + altNumMatcher.group(1).trim());
                }
            }

            if (dateMatcher.find()) {
                headerInfo.put("report_date", dateMatcher.group(1).trim());
                System.out.println("Found date: " + dateMatcher.group(1).trim());
            } else {
                // Try alternate pattern
                Pattern altDatePattern = Pattern.compile("Date\\s*[:.=]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
                Matcher altDateMatcher = altDatePattern.matcher(fullText);
                if (altDateMatcher.find()) {
                    headerInfo.put("report_date", altDateMatcher.group(1).trim());
                    System.out.println("Found date (alt): " + altDateMatcher.group(1).trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting report number/date: " + e.getMessage());
            e.printStackTrace();
        }

        // Depth and TVD
        try {
            Pattern depthPattern = Pattern.compile("Depth\\s*@\\s*24h\\s*[:.=]\\s*(\\d+)");
            Pattern tvdPattern = Pattern.compile("TVD\\s*[:.=]\\s*(\\d+)");

            Matcher depthMatcher = depthPattern.matcher(fullText);
            Matcher tvdMatcher = tvdPattern.matcher(fullText);

            if (depthMatcher.find()) {
                headerInfo.put("depth_24h_ft", depthMatcher.group(1).trim());
                System.out.println("Found depth: " + depthMatcher.group(1).trim());
            }

            if (tvdMatcher.find()) {
                headerInfo.put("tvd_ft", tvdMatcher.group(1).trim());
                System.out.println("Found TVD: " + tvdMatcher.group(1).trim());
            }
        } catch (Exception e) {
            System.err.println("Error extracting depth/TVD: " + e.getMessage());
            e.printStackTrace();
        }

        // Progress
        try {
            Pattern progressPattern = Pattern.compile("PROGRESS\\s*[:.=]\\s*(\\d+)\\s*ft");
            Pattern hoursPattern = Pattern.compile("in\\s*(\\d+\\.?\\d*)\\s*h");

            Matcher progressMatcher = progressPattern.matcher(fullText);
            Matcher hoursMatcher = hoursPattern.matcher(fullText);

            if (progressMatcher.find()) {
                headerInfo.put("progress_ft", progressMatcher.group(1).trim());
                System.out.println("Found progress: " + progressMatcher.group(1).trim());
            }

            if (hoursMatcher.find()) {
                headerInfo.put("progress_hours", hoursMatcher.group(1).trim());
                System.out.println("Found hours: " + hoursMatcher.group(1).trim());
            }
        } catch (Exception e) {
            System.err.println("Error extracting progress: " + e.getMessage());
            e.printStackTrace();
        }

        // Casing
        try {
            if (fullText.contains("Last Casing")) {
                System.out.println("Found Last Casing section");
                String[] parts = fullText.split("Last Casing");
                if (parts.length > 1) {
                    String casingSection = parts[1].split("\n")[0];
                    System.out.println("Casing section: " + casingSection);

                    String[] casingParts = casingSection.split("AT:");
                    String casingType = casingParts[0].replace("Liner :", "").trim();
                    casingType = casingType.replaceAll("nan", "").trim();

                    if (!casingType.isEmpty()) {
                        headerInfo.put("last_casing", casingType);
                        System.out.println("Found casing type: " + casingType);
                    }

                    Pattern atPattern = Pattern.compile("AT:\\s*(\\d+)");
                    Matcher atMatcher = atPattern.matcher(casingSection);
                    if (atMatcher.find()) {
                        headerInfo.put("casing_at_ft", atMatcher.group(1).trim());
                        System.out.println("Found casing at: " + atMatcher.group(1).trim());
                    }

                    Pattern topAtPattern = Pattern.compile("Top\\s+AT:\\s*(\\d*)");
                    Matcher topAtMatcher = topAtPattern.matcher(casingSection);
                    if (topAtMatcher.find()) {
                        String topAt = topAtMatcher.group(1).trim();
                        if (!topAt.isEmpty()) {
                            headerInfo.put("casing_top_at_ft", topAt);
                            System.out.println("Found casing top at: " + topAt);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting casing info: " + e.getMessage());
            e.printStackTrace();
        }

        return headerInfo;
    }

    /**
     * Fallback method to find well name anywhere in the sheet
     */
    private static String findWellNameInSheet(Sheet sheet) {
        Pattern wellPattern = Pattern.compile("(?i)\\b(?:well|puits)\\s*(?:name|nom)?\\s*[:=]\\s*([\\w\\s-]+)");
        String fullText = sheetToString(sheet);

        Matcher matcher = wellPattern.matcher(fullText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    

    private static String sheetToString(Sheet sheet) {
        StringBuilder sb = new StringBuilder();
        int numRows = sheet.getLastRowNum();
        for (int i = 0; i <= numRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        sb.append(getCellValueAsString(cell)).append(" ");
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (IllegalStateException e2) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    /**
     * Analyzes drilling parameters from dataframe-like data structure.
     * Translation of Python implementation to Java.
     *
     * @param sheet The Excel sheet containing drilling parameters
     * @return JSONObject containing the parsed drilling parameters
     */
    public static JSONObject parseDrillingParameters(Sheet sheet) {
        JSONObject params = new JSONObject();

        // Initialize parameters with null values
        params.put("bit_number", JSONObject.NULL);
        params.put("bit_size", JSONObject.NULL);
        params.put("bit_type", JSONObject.NULL);
        params.put("bit_serial", JSONObject.NULL);
        params.put("nozzles", new JSONArray());
        params.put("tfa_sqin", JSONObject.NULL);
        params.put("hsi_hp_sqin", JSONObject.NULL);
        params.put("wob_min_t", JSONObject.NULL);
        params.put("wob_max_t", JSONObject.NULL);
        params.put("rpm_min", JSONObject.NULL);
        params.put("rpm_max", JSONObject.NULL);
        params.put("flow_gpm", JSONObject.NULL);
        params.put("pressure_psi", JSONObject.NULL);
        params.put("progress_ft", JSONObject.NULL);
        params.put("progress_hours", JSONObject.NULL);
        params.put("cumulative_ft", JSONObject.NULL);
        params.put("cumulative_hours", JSONObject.NULL);
        params.put("deviation_depth_ft", JSONObject.NULL);
        params.put("deviation_inclination", JSONObject.NULL);
        params.put("deviation_azimuth", JSONObject.NULL);
        params.put("deviation_tvd_ft", JSONObject.NULL);

        try {
            // Convert sheet to text and extract lines
            String fullText = sheetToString(sheet);
            String[] lines = fullText.split("\n");

            // Find the data line containing the drilling parameters pattern (03 U 6)
            String dataLine = null;
            for (String line : lines) {
                if (line.matches(".*03\\s+U\\s+6.*")) {
                    dataLine = line;
                    break;
                }
            }

            if (dataLine != null) {
                System.out.println("Found drilling parameters data line: " + dataLine);

                // Split line by multiple spaces to get data parts
                String[] dataParts = dataLine.trim().split("\\s{2,}");

                // Define mapping of field positions
                java.util.Map<String, Integer> fieldPositions = new java.util.HashMap<>();
                fieldPositions.put("bit_number", 0);
                fieldPositions.put("bit_size", 3);
                fieldPositions.put("bit_type", 4);
                fieldPositions.put("bit_serial", 5);
                fieldPositions.put("nozzles_start", 6);
                fieldPositions.put("nozzles_end", 12);
                fieldPositions.put("tfa_sqin", 13);
                fieldPositions.put("hsi_hp_sqin", 14);
                fieldPositions.put("wob_min_t", 15);
                fieldPositions.put("wob_max_t", 16);
                fieldPositions.put("rpm_min", 17);
                fieldPositions.put("rpm_max", 18);
                fieldPositions.put("flow_gpm", 19);
                fieldPositions.put("pressure_psi", 20);
                fieldPositions.put("progress_ft", 21);
                fieldPositions.put("progress_hours", 22);

                // Extract fixed position values
                for (java.util.Map.Entry<String, Integer> entry : fieldPositions.entrySet()) {
                    String field = entry.getKey();
                    int pos = entry.getValue();

                    if (!field.contains("nozzles")) {
                        if (pos < dataParts.length) {
                            String val = cleanValue(dataParts[pos]);
                            if (val != null) {
                                if (isNumeric(val)) {
                                    if (val.contains(".")) {
                                        params.put(field, Double.parseDouble(val));
                                    } else {
                                        params.put(field, Integer.parseInt(val));
                                    }
                                } else {
                                    params.put(field, val);
                                }
                            }
                        }
                    }
                }

                // Extract nozzles
                JSONArray nozzles = new JSONArray();
                int nozzlesStart = fieldPositions.get("nozzles_start");
                int nozzlesEnd = fieldPositions.get("nozzles_end");

                for (int i = nozzlesStart; i <= nozzlesEnd; i++) {
                    if (i < dataParts.length) {
                        String val = cleanValue(dataParts[i]);
                        if (val != null) {
                            nozzles.put(val);
                        }
                    }
                }
                params.put("nozzles", nozzles);

                // Process deviation fields
                int deviationStart = 25;
                String[] deviationFields = {
                        "deviation_depth_ft",
                        "deviation_inclination",
                        "deviation_azimuth",
                        "deviation_tvd_ft"
                };

                for (int i = 0; i < deviationFields.length; i++) {
                    int pos = deviationStart + i;
                    if (pos < dataParts.length) {
                        String val = cleanValue(dataParts[pos]);
                        if (val != null) {
                            if (isNumeric(val)) {
                                if (val.contains(".")) {
                                    params.put(deviationFields[i], Double.parseDouble(val));
                                } else {
                                    params.put(deviationFields[i], Integer.parseInt(val));
                                }
                            } else {
                                params.put(deviationFields[i], val);
                            }
                        }
                    }
                }

                // Copy progress values to cumulative
                if (params.opt("progress_ft") != JSONObject.NULL) {
                    params.put("cumulative_ft", params.opt("progress_ft"));
                }
                if (params.opt("progress_hours") != JSONObject.NULL) {
                    params.put("cumulative_hours", params.opt("progress_hours"));
                }
            } else {
                System.out.println("No drilling parameters data line found");
            }

        } catch (Exception e) {
            System.err.println("Error parsing drilling parameters: " + e.getMessage());
            e.printStackTrace();
        }

        return params;
    }

    public static JSONObject parseMudInformation(Sheet sheet) {
        // Create the main structure initialized with null values
        JSONObject mudInfo = new JSONObject();

        // MUD_PROPERTIES section
        JSONObject mudProperties = new JSONObject();
        String[] mudPropsKeys = {
                "MUD_TYPE", "F_API", "FUNNEL", "Y_POINT", "APP_VIS", "PV", "WEIGHT",
                "F_HPHT", "PH", "GEL_10_SEC", "GEL_10_MIN", "MBT_PPB", "KCL_PERCENT",
                "WATER_PERCENT", "PF", "CACL2", "SOLIDS_PERCENT", "SAND_PERCENT",
                "CA_PLUS_PLUS", "NACL", "MF", "CL_G_L", "LGS_PERCENT", "HGS_PERCENT",
                "E_LIME", "CAKE"
        };
        for (String key : mudPropsKeys) {
            mudProperties.put(key, JSONObject.NULL);
        }
        mudInfo.put("MUD_PROPERTIES", mudProperties);

        // CENTRIFUGE_SERVICE section
        JSONObject centrifugeService = new JSONObject();
        String[] centrifugeKeys = {"NUMBER", "MODEL", "HOURS", "BRAND"};
        for (String key : centrifugeKeys) {
            centrifugeService.put(key, JSONObject.NULL);
        }
        mudInfo.put("CENTRIFUGE_SERVICE", centrifugeService);

        // MUD_CLEANER_SERVICE section
        mudInfo.put("MUD_CLEANER_SERVICE", JSONObject.NULL);

        // PITS section
        JSONObject pits = new JSONObject();
        String[] pitsKeys = {"BEHIND_CSG", "OTHER_HOLE", "LOST_HOLE", "LOST_SURF", "DUMPED"};
        for (String key : pitsKeys) {
            pits.put(key, JSONObject.NULL);
        }
        mudInfo.put("PITS", pits);

        // VOLUMES_BBL section
        JSONObject volumesBbl = new JSONObject();
        String[] volumesKeys = {"ACTIVE", "RESERVE", "KILL_MUD", "AVAILABLE"};
        for (String key : volumesKeys) {
            volumesBbl.put(key, JSONObject.NULL);
        }
        mudInfo.put("VOLUMES_BBL", volumesBbl);

        // LOSSES section
        JSONObject losses = new JSONObject();
        losses.put("SCE_LOSSES", JSONObject.NULL);
        mudInfo.put("LOSSES", losses);

        // STATUS section
        JSONObject status = new JSONObject();
        String[] statusKeys = {"TRIPPING", "ENCAPSULATION", "SHAKERS", "LOST_CIRC"};
        for (String key : statusKeys) {
            status.put(key, JSONObject.NULL);
        }
        mudInfo.put("STATUS", status);

        try {
            // Extract rows 13 to 20 (index 12 to 19 in 0-based)
            StringBuilder fullText = new StringBuilder();
            for (int rowNum = 12; rowNum < 20; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        if (cell != null) {
                            fullText.append(getCellValueAsString(cell)).append(" ");
                        }
                    }
                    fullText.append("\n");
                }
            }

            String mudText = fullText.toString();
            System.out.println("Extracted mud information text, length: " + mudText.length());

            // Define patterns for all fields
            java.util.Map<Pattern, String[]> patterns = new java.util.HashMap<>();

            // MUD PROPERTIES patterns
            patterns.put(Pattern.compile("MUD TYPE:\\s*([^\\s]+)"), new String[]{"MUD_PROPERTIES", "MUD_TYPE"});
            patterns.put(Pattern.compile("F\\.API:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "F_API"});
            patterns.put(Pattern.compile("FUNNEL:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "FUNNEL"});
            patterns.put(Pattern.compile("Y\\.Point:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "Y_POINT"});
            patterns.put(Pattern.compile("App\\. Vis:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "APP_VIS"});
            patterns.put(Pattern.compile("PV:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "PV"});
            patterns.put(Pattern.compile("WEIGHT:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "WEIGHT"});
            patterns.put(Pattern.compile("F\\.HPHT:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "F_HPHT"});
            patterns.put(Pattern.compile("PH:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "PH"});
            patterns.put(Pattern.compile("GEL 10'':\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "GEL_10_SEC"});
            patterns.put(Pattern.compile("GEL 10':\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "GEL_10_MIN"});
            patterns.put(Pattern.compile("M\\.BT\\. PPB.*?([\\d.]+)"), new String[]{"MUD_PROPERTIES", "MBT_PPB"});
            patterns.put(Pattern.compile("%KCL:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "KCL_PERCENT"});
            patterns.put(Pattern.compile("%W:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "WATER_PERCENT"});
            patterns.put(Pattern.compile("Pf:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "PF"});
            patterns.put(Pattern.compile("Cacl2:\\s*([\\d.]+|N/A)"), new String[]{"MUD_PROPERTIES", "CACL2"});
            patterns.put(Pattern.compile("%Solids:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "SOLIDS_PERCENT"});
            patterns.put(Pattern.compile("%Sand:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "SAND_PERCENT"});
            patterns.put(Pattern.compile("Ca\\+\\+:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "CA_PLUS_PLUS"});
            patterns.put(Pattern.compile("NaCI\\s*([^\\s]+)"), new String[]{"MUD_PROPERTIES", "NACL"});
            patterns.put(Pattern.compile("MF:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "MF"});
            patterns.put(Pattern.compile("CL- g/l\\s*:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "CL_G_L"});
            patterns.put(Pattern.compile("%LGS:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "LGS_PERCENT"});
            patterns.put(Pattern.compile("%HGS:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "HGS_PERCENT"});
            patterns.put(Pattern.compile("E-Lime:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "E_LIME"});
            patterns.put(Pattern.compile("Cake\\s*:\\s*([\\d.]+)"), new String[]{"MUD_PROPERTIES", "CAKE"});

            // CENTRIFUGE SERVICE patterns
            patterns.put(Pattern.compile("N°\\s*(\\d+)"), new String[]{"CENTRIFUGE_SERVICE", "NUMBER"});
            patterns.put(Pattern.compile("Model\\s*([^\\s]+)"), new String[]{"CENTRIFUGE_SERVICE", "MODEL"});
            patterns.put(Pattern.compile("Hours\\s*([\\d.]+)"), new String[]{"CENTRIFUGE_SERVICE", "HOURS"});
            patterns.put(Pattern.compile("BRAND\\s*([^\\s]+)"), new String[]{"CENTRIFUGE_SERVICE", "BRAND"});

            // MUD CLEANER pattern
            patterns.put(Pattern.compile("MUD CLEANER SERVICE\\s*=\\s*([^\\s]+)"), new String[]{"MUD_CLEANER_SERVICE", null});

            // PITS patterns
            patterns.put(Pattern.compile("Behind Csg:\\s*([^\\s]+)"), new String[]{"PITS", "BEHIND_CSG"});
            patterns.put(Pattern.compile("Other \\(Hole\\):\\s*([^\\s]+)"), new String[]{"PITS", "OTHER_HOLE"});
            patterns.put(Pattern.compile("Lost Hole:\\s*([^\\s]+)"), new String[]{"PITS", "LOST_HOLE"});
            patterns.put(Pattern.compile("Lost Surf:\\s*([^\\s]+)"), new String[]{"PITS", "LOST_SURF"});
            patterns.put(Pattern.compile("Dumped:\\s*([^\\s]+)"), new String[]{"PITS", "DUMPED"});

            // VOLUMES patterns
            patterns.put(Pattern.compile("Active\\s*([\\d.]+)"), new String[]{"VOLUMES_BBL", "ACTIVE"});
            patterns.put(Pattern.compile("Reserve\\s*([\\d.]+)"), new String[]{"VOLUMES_BBL", "RESERVE"});
            patterns.put(Pattern.compile("Kill Mud\\s*([\\d.]+)"), new String[]{"VOLUMES_BBL", "KILL_MUD"});
            patterns.put(Pattern.compile("Hole\\s*([\\d.]+)"), new String[]{"VOLUMES_BBL", "AVAILABLE"});

            // LOSSES patterns
            patterns.put(Pattern.compile("S\\.C\\.E\\. Losses\\s*([\\d.]+)"), new String[]{"LOSSES", "SCE_LOSSES"});

            // STATUS patterns
            patterns.put(Pattern.compile("Tripping:\\s*([^\\s]+)"), new String[]{"STATUS", "TRIPPING"});
            patterns.put(Pattern.compile("Encapsulation:\\s*([^\\s]+)"), new String[]{"STATUS", "ENCAPSULATION"});
            patterns.put(Pattern.compile("Shakers:\\s*([^\\s]+)"), new String[]{"STATUS", "SHAKERS"});
            patterns.put(Pattern.compile("Lost Circ:\\s*([^\\s]+)"), new String[]{"STATUS", "LOST_CIRC"});

            // Extract values using the patterns
            for (Map.Entry<Pattern, String[]> entry : patterns.entrySet()) {
                Pattern pattern = entry.getKey();
                String[] location = entry.getValue();

                Matcher matcher = pattern.matcher(mudText);
                if (matcher.find()) {
                    String value = cleanValue(matcher.group(1));
                    if (value != null) {
                        String section = location[0];
                        String key = location[1];

                        if ("MUD_CLEANER_SERVICE".equals(section)) {
                            if (!"N/A".equalsIgnoreCase(value)) {
                                mudInfo.put(section, value);
                            }
                        } else if (key != null) {
                            JSONObject sectionObj = mudInfo.getJSONObject(section);
                            if (isNumeric(value)) {
                                if (value.contains(".")) {
                                    sectionObj.put(key, Double.parseDouble(value));
                                } else {
                                    sectionObj.put(key, Integer.parseInt(value));
                                }
                            } else if (!"N/A".equalsIgnoreCase(value)) {
                                sectionObj.put(key, value);
                            }
                        }
                    }
                }
            }

            System.out.println("Mud information parsing complete");

        } catch (Exception e) {
            System.err.println("Error parsing mud information: " + e.getMessage());
            e.printStackTrace();
        }

        return mudInfo;
    }

    /**
     * Cleans a value by trimming and replacing empty or special values with null
     */
    private static String cleanValue(String value) {
        if (value == null) return null;

        String cleaned = value.trim();
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("nan") ||
                cleaned.equalsIgnoreCase("None") || cleaned.equalsIgnoreCase("null")) {
            return null;
        }
        return cleaned;
    }

    /**
     * Checks if a string is numeric (integer or decimal)
     */
    private static boolean isNumeric(String str) {
        if (str == null) return false;

        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * Method to parse drilling operations from Excel sheet.
     * Translated from Python to Java based on parse_operations method.
     *
     * @param sheet The Excel sheet containing operations information
     * @return JSONArray containing the parsed operations
     */
    /**
     * Method to parse drilling operations from Excel sheet.
     * Translated from Python to Java based on parse_operations method.
     *
     * @param sheet The Excel sheet containing operations information
     * @return JSONObject containing the parsed operations
     */
    public static JSONObject parseOperations(Sheet sheet) {
        JSONObject result = new JSONObject();
        JSONArray operations = new JSONArray();
        Integer headerRow = null;
        Map<String, Integer> colPos = new HashMap<>();

        try {
            // 1. Find the header row by looking for specific column names
            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append("|");
                    }
                }

                String rowText = rowStr.toString().toUpperCase().trim();

                if (rowText.contains("TIMING") && rowText.contains("OPERATION") && rowText.contains("DESCRIPTION")) {
                    headerRow = i;

                    // Find column positions
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell).replace("\n", " ").toUpperCase().trim();

                            if (cellValue.contains("OPERATION DESCRIPTION")) {
                                colPos.put("desc", j);
                            } else if (cellValue.contains("CODE")) {
                                colPos.put("code", j);
                            } else if (cellValue.contains("COMPANY")) {
                                colPos.put("company", j);
                            } else if (cellValue.contains("RATE")) {
                                colPos.put("rate", j);
                            } else if (cellValue.contains("INITIAL DEPTH")) {
                                colPos.put("init_depth", j);
                            } else if (cellValue.contains("FINAL DEPTH")) {
                                colPos.put("final_depth", j);
                            }
                        }
                    }
                    break;
                }
            }

            if (headerRow == null) {
                System.out.println("Header row not found in operations parsing");
                result.put("operations", operations);
                return result;
            }

            System.out.println("Found header row at index: " + headerRow);
            System.out.println("Column positions: " + colPos);

            // 2. Extract operations
            for (int i = headerRow + 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Build row string to check for special cases
                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append(" ");
                    }
                }

                String rowText = rowStr.toString().trim();

                // Skip empty rows or remarks section
                if (rowText.isEmpty() || rowText.toUpperCase().contains("REMARKS")) {
                    continue;
                }

                // Handle special case for "AFTER MIDNIGHT"
                if (rowText.toUpperCase().contains("AFTER MIDNIGHT")) {
                    JSONObject operation = new JSONObject();
                    operation.put("start_time", "");
                    operation.put("end_time", "");
                    operation.put("description", "AFTER MIDNIGHT");
                    operation.put("code", "");
                    operation.put("initial_depth", "");
                    operation.put("final_depth", "");
                    operation.put("company", "");
                    operation.put("rate", "");

                    operations.put(operation);
                    continue;
                }

                // Find start_time and end_time in the first 10 cells
                List<String> times = new ArrayList<>();
                for (int j = 0; j < Math.min(10, row.getLastCellNum()); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String value = getCellValueAsString(cell).trim();
                        if (value.matches("\\d{2}:\\d{2}")) {
                            times.add(value);
                        }
                    }
                }

                String startTime = times.size() > 0 ? times.get(0) : "";
                String endTime = times.size() > 1 ? times.get(1) : "";

                // Get values for each field
                String description = getValueFromCell(row, colPos, "desc", "");
                String code = getValueFromCell(row, colPos, "code", "");
                String company = getValueFromCell(row, colPos, "company", "");
                String initDepth = getValueFromCell(row, colPos, "init_depth", "");
                String finalDepth = getValueFromCell(row, colPos, "final_depth", "");
                String rate = getValueFromCell(row, colPos, "rate", "");

                // Create operation object
                JSONObject operation = new JSONObject();
                operation.put("start_time", startTime);
                operation.put("end_time", endTime);
                operation.put("description", description);
                operation.put("code", code);
                operation.put("initial_depth", initDepth);
                operation.put("final_depth", finalDepth);
                operation.put("company", company);
                operation.put("rate", rate);

                operations.put(operation);
            }

            System.out.println("Extracted " + operations.length() + " operations");

        } catch (Exception e) {
            System.err.println("Error parsing operations: " + e.getMessage());
            e.printStackTrace();
        }

        // Put the operations array into the result object
        result.put("operations", operations);

        return result;
    }

    /**
     * Helper method to get value from a specific column in a row
     *
     * @param row The Excel row
     * @param colPos Map of column names to positions
     * @param colName The column name to look for
     * @param defaultValue Default value if column not found or value is empty
     * @return The cell value as string or defaultValue
     */
    private static String getValueFromCell(Row row, Map<String, Integer> colPos, String colName, String defaultValue) {
        if (!colPos.containsKey(colName)) {
            return defaultValue;
        }

        int colIndex = colPos.get(colName);
        if (colIndex >= row.getLastCellNum()) {
            return defaultValue;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return defaultValue;
        }

        String value = getCellValueAsString(cell).trim();
        if (value.isEmpty() || value.equalsIgnoreCase("nan")) {
            return defaultValue;
        }

        return value;
    }



    /**
     * Method to parse BHA (Bottom Hole Assembly) report from Excel sheet.
     * Translated from Python to Java.
     *
     * @param sheet The Excel sheet containing BHA information
     * @return JSONObject containing the parsed BHA data
     */
    public static JSONObject parseBHAReport(Sheet sheet) {
        JSONObject result = new JSONObject();
        JSONArray downholeEquipment = new JSONArray();
        JSONArray bhaComponents = new JSONArray();

        // Initialize the result structure
        result.put("downhole_equipment", downholeEquipment);
        result.put("bha_components", bhaComponents);
        result.put("total_length", JSONObject.NULL);

        try {
            // Find both sections
            Integer downholeStart = null;
            Integer bhaStart = null;

            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append(" ");
                    }
                }

                String rowText = rowStr.toString().trim();

                if (rowText.contains("Down hole eqts") && downholeStart == null) {
                    downholeStart = i;
                }
                if (rowText.contains("BHA Components")) {
                    bhaStart = i;
                    break;  // Once we find BHA, we can stop searching
                }
            }

            // 1. Extract Downhole Equipment
            if (downholeStart != null) {
                // In the original Python code, the headers are on the same row as "Down hole eqts"
                int headerRow = downholeStart;

                // Find column positions
                Integer equipmentCol = null;
                Integer serialCol = null;
                Integer hoursCol = null;

                Row headerRowObj = sheet.getRow(headerRow);
                if (headerRowObj != null) {
                    for (int col = 0; col < headerRowObj.getLastCellNum(); col++) {
                        Cell cell = headerRowObj.getCell(col);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        if (cellValue.contains("Down hole eqts")) {
                            equipmentCol = col;
                        } else if (cellValue.contains("SERIAL")) {
                            serialCol = col;
                        } else if (cellValue.contains("HOURS")) {
                            hoursCol = col;
                        }
                    }
                }

                // If columns not found, use default positions (0, 1, 2)
                if (equipmentCol == null) {
                    equipmentCol = 0;
                }
                if (serialCol == null) {
                    serialCol = 1;
                }
                if (hoursCol == null) {
                    hoursCol = 2;
                }

                // Extract data starting from next row
                int startIdx = headerRow + 1;
                int endIdx = (bhaStart != null) ? bhaStart : lastRow;

                for (int idx = startIdx; idx <= endIdx; idx++) {
                    Row dataRow = sheet.getRow(idx);
                    if (dataRow == null) continue;

                    String equipmentName = "";
                    if (equipmentCol < dataRow.getLastCellNum()) {
                        Cell cell = dataRow.getCell(equipmentCol);
                        if (cell != null) {
                            equipmentName = getCellValueAsString(cell).trim();
                        }
                    }

                    // Skip empty rows or rows that might be headers
                    if (equipmentName.isEmpty() ||
                            equipmentName.contains("Down hole eqts") ||
                            equipmentName.contains("SERIAL") ||
                            equipmentName.contains("HOURS") ||
                            equipmentName.contains("BHA Components")) {
                        continue;
                    }

                    // Create equipment entry
                    JSONObject equipment = new JSONObject();
                    equipment.put("Equipment", equipmentName);

                    String serial = "";
                    if (serialCol < dataRow.getLastCellNum()) {
                        Cell cell = dataRow.getCell(serialCol);
                        if (cell != null) {
                            serial = getCellValueAsString(cell).trim();
                        }
                    }
                    equipment.put("SERIAL", serial);

                    String hours = "";
                    if (hoursCol < dataRow.getLastCellNum()) {
                        Cell cell = dataRow.getCell(hoursCol);
                        if (cell != null) {
                            hours = getCellValueAsString(cell).trim();
                        }
                    }
                    equipment.put("HOURS", hours);

                    downholeEquipment.put(equipment);
                }
            }

            // 2. Extract BHA Components
            if (bhaStart != null) {
                // Find columns for BHA Components
                Integer bhaNumCol = null;
                Integer itemCol = null;
                Integer jtsCol = null;
                Integer lenFtCol = null;
                Integer idInCol = null;
                Integer odInCol = null;

                // Look for the headers row
                Integer headerRow = null;
                for (int idx = bhaStart; idx <= Math.min(bhaStart + 5, lastRow); idx++) {
                    Row row = sheet.getRow(idx);
                    if (row == null) continue;

                    for (int col = 0; col < row.getLastCellNum(); col++) {
                        Cell cell = row.getCell(col);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        if (cellValue.contains("BHA N°") || cellValue.contains("BHA N")) {
                            bhaNumCol = col;
                            headerRow = idx;
                        } else if (cellValue.contains("Item")) {
                            itemCol = col;
                        } else if (cellValue.contains("Jts")) {
                            jtsCol = col;
                        } else if (cellValue.contains("Len (ft)") || cellValue.contains("Len(ft)") ||
                                cellValue.contains("Length") || cellValue.contains("Len ft")) {
                            lenFtCol = col;
                        } else if (cellValue.contains("I.D (in)") || cellValue.contains("I.D(in)") ||
                                cellValue.contains("ID (in)") || cellValue.contains("ID(in)") ||
                                cellValue.contains("I.D")) {
                            idInCol = col;
                        } else if (cellValue.contains("O.D (in)") || cellValue.contains("O.D(in)") ||
                                cellValue.contains("OD (in)") || cellValue.contains("OD(in)") ||
                                cellValue.contains("O.D")) {
                            odInCol = col;
                        }
                    }
                }

                // Extract component data
                if (headerRow != null && bhaNumCol != null) {
                    for (int idx = headerRow + 1; idx <= lastRow; idx++) {
                        Row currentRow = sheet.getRow(idx);
                        if (currentRow == null) continue;

                        // Check for Total length row
                        boolean totalLengthFound = false;
                        for (int col = 0; col < currentRow.getLastCellNum(); col++) {
                            Cell cell = currentRow.getCell(col);
                            if (cell == null) continue;

                            String cellValue = getCellValueAsString(cell).trim();
                            if (cellValue.contains("Total length")) {
                                totalLengthFound = true;
                                // Look for the length value in this row
                                for (int c = col; c < currentRow.getLastCellNum(); c++) {
                                    Cell valueCell = currentRow.getCell(c);
                                    if (valueCell == null) continue;

                                    String value = getCellValueAsString(valueCell).trim();
                                    if (!value.isEmpty() && value.matches(".*\\d.*")) {  // Contains at least one digit
                                        // Extract numeric part
                                        String numericValue = value.replaceAll("[^0-9.]", "");
                                        result.put("total_length", numericValue);
                                        break;
                                    }
                                }
                                break;
                            }
                        }

                        if (totalLengthFound) {
                            break;
                        }

                        // Extract BHA component if BHA number column exists in this row
                        if (bhaNumCol >= currentRow.getLastCellNum()) continue;

                        Cell bhaNumCell = currentRow.getCell(bhaNumCol);
                        if (bhaNumCell == null) continue;

                        String bhaNum = getCellValueAsString(bhaNumCell).trim();

                        // Check if bhaNum is a valid number
                        if (!bhaNum.isEmpty() && bhaNum.matches("^\\d+(\\.\\d+)?$")) {
                            JSONObject component = new JSONObject();
                            component.put("BHA_N°", bhaNum);

                            // Item
                            if (itemCol != null && itemCol < currentRow.getLastCellNum()) {
                                Cell cell = currentRow.getCell(itemCol);
                                String item = (cell != null) ? getCellValueAsString(cell).trim() : "";
                                component.put("Item", item);
                            } else {
                                component.put("Item", "");
                            }

                            // Jts
                            if (jtsCol != null && jtsCol < currentRow.getLastCellNum()) {
                                Cell cell = currentRow.getCell(jtsCol);
                                String jts = (cell != null) ? getCellValueAsString(cell).trim() : "";
                                component.put("Jts", jts);
                            } else {
                                component.put("Jts", "");
                            }

                            // Length (ft)
                            if (lenFtCol != null && lenFtCol < currentRow.getLastCellNum()) {
                                Cell cell = currentRow.getCell(lenFtCol);
                                String lenFt = (cell != null) ? getCellValueAsString(cell).trim() : "";
                                component.put("Len_ft", lenFt);
                            } else {
                                component.put("Len_ft", "");
                            }

                            // ID (in)
                            if (idInCol != null && idInCol < currentRow.getLastCellNum()) {
                                Cell cell = currentRow.getCell(idInCol);
                                String idIn = (cell != null) ? getCellValueAsString(cell).trim().replaceAll("[^0-9.]", "") : "";
                                component.put("ID_in", idIn);
                            } else {
                                component.put("ID_in", "");
                            }

                            // OD (in)
                            if (odInCol != null && odInCol < currentRow.getLastCellNum()) {
                                Cell cell = currentRow.getCell(odInCol);
                                String odIn = (cell != null) ? getCellValueAsString(cell).trim().replaceAll("[^0-9.]", "") : "";
                                component.put("OD_in", odIn);
                            } else {
                                component.put("OD_in", "");
                            }

                            bhaComponents.put(component);
                        }
                    }
                }
            }

            System.out.println("Extracted " + downholeEquipment.length() + " downhole equipment items");
            System.out.println("Extracted " + bhaComponents.length() + " BHA components");

        } catch (Exception e) {
            System.err.println("Error parsing BHA report: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }


    /**
     * Method to parse mud products information from Excel sheet.
     * Translated from Python to Java.
     *
     * @param sheet The Excel sheet containing mud products information
     * @return JSONArray containing the parsed mud products data
     */
    public static JSONArray parseMudProducts(Sheet sheet) {
        JSONArray products = new JSONArray();

        try {
            // Find the mud products section
            boolean productsSectionFound = false;
            Integer productsHeaderRow = null;
            Integer productsCol = null;
            Integer usedCol = null;
            Integer storedCol = null;
            Integer unitCol = null;

            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append(" ");
                    }
                }

                String rowText = rowStr.toString().trim();

                if (rowText.contains("MUD PRODUCTS")) {
                    productsSectionFound = true;
                    productsHeaderRow = i;

                    // Find the indices of important columns
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        if (cellValue.contains("MUD PRODUCTS")) {
                            productsCol = j;
                        } else if (cellValue.contains("USED")) {
                            usedCol = j;
                        } else if (cellValue.contains("STORED")) {
                            storedCol = j;
                        } else if (cellValue.contains("UNIT")) {
                            unitCol = j;
                        }
                    }

                    break;
                }
            }

            // If the products section was found, extract the products
            if (productsSectionFound && productsHeaderRow != null) {
                int currentRow = productsHeaderRow + 1;

                while (currentRow <= lastRow && currentRow < productsHeaderRow + 30) { // Limit to 30 products maximum
                    Row row = sheet.getRow(currentRow);
                    if (row == null) {
                        currentRow++;
                        continue;
                    }

                    // Check if it's a valid product row
                    if (productsCol != null && productsCol < row.getLastCellNum()) {
                        Cell productCell = row.getCell(productsCol);
                        String productName = (productCell != null) ? getCellValueAsString(productCell).trim() : "";

                        // Check if it's a valid product (non-empty name)
                        if (!productName.isEmpty() && !productName.equals("nan")) {
                            String usedValue = "";
                            if (usedCol != null && usedCol < row.getLastCellNum()) {
                                Cell cell = row.getCell(usedCol);
                                usedValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                            }

                            String storedValue = "";
                            if (storedCol != null && storedCol < row.getLastCellNum()) {
                                Cell cell = row.getCell(storedCol);
                                storedValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                            }

                            String unitValue = "";
                            if (unitCol != null && unitCol < row.getLastCellNum()) {
                                Cell cell = row.getCell(unitCol);
                                unitValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                            }

                            // Clean values
                            if (usedValue.equals("nan")) {
                                usedValue = "";
                            }
                            if (storedValue.equals("nan")) {
                                storedValue = "";
                            }
                            if (unitValue.equals("nan")) {
                                unitValue = "";
                            }

                            JSONObject product = new JSONObject();
                            product.put("name", productName);
                            product.put("used", usedValue);
                            product.put("stored", storedValue);
                            product.put("unit", unitValue);

                            products.put(product);
                        }
                    }

                    currentRow++;
                }
            }

            System.out.println("Extracted " + products.length() + " mud products");

        } catch (Exception e) {
            System.err.println("Error parsing mud products: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }


    /**
     * Method to parse lithology information from Excel sheet.
     * Translated from Python to Java.
     *
     * @param sheet The Excel sheet containing lithology information
     * @return JSONArray containing the parsed lithology data
     */
    public static JSONArray parseLithology(Sheet sheet) {
        JSONArray lithologyData = new JSONArray();

        try {
            // Find the lithology section
            boolean lithoSectionFound = false;
            Integer lithoHeaderRow = null;
            Integer fromCol = null;
            Integer toCol = null;
            Integer intervalCol = null;
            Integer wcCol = null;
            Integer wocCol = null;
            Integer stageCol = null;

            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append(" ");
                    }
                }

                String rowText = rowStr.toString().trim();

                if (rowText.contains("LITHOLOGY") && (rowText.contains("ROP") || rowText.contains("From ft"))) {
                    lithoSectionFound = true;
                    lithoHeaderRow = i;

                    // Check headers in the next row
                    if (i + 1 <= lastRow) {
                        Row headerRow = sheet.getRow(i + 1);
                        if (headerRow != null) {
                            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                                Cell cell = headerRow.getCell(j);
                                if (cell == null) continue;

                                String cellValue = getCellValueAsString(cell).trim();

                                if (cellValue.contains("From")) {
                                    fromCol = j;
                                } else if (cellValue.contains("To")) {
                                    toCol = j;
                                } else if (cellValue.contains("Total Int")) {
                                    intervalCol = j;
                                } else if (cellValue.contains("wc")) {
                                    wcCol = j;
                                } else if (cellValue.contains("woc")) {
                                    wocCol = j;
                                } else if (cellValue.contains("Stage")) {
                                    stageCol = j;
                                }
                            }
                        }
                    }

                    break;
                }
            }

            // If the lithology section was found, extract the data
            if (lithoSectionFound && lithoHeaderRow != null && fromCol != null && toCol != null) {
                int currentRow = lithoHeaderRow + 2;  // Start after the headers

                while (currentRow <= lastRow && currentRow < lithoHeaderRow + 15) {  // Limit to 15 entries maximum
                    Row row = sheet.getRow(currentRow);
                    if (row == null) {
                        currentRow++;
                        continue;
                    }

                    // Check if it's a valid lithology row
                    String fromValue = "";
                    if (fromCol < row.getLastCellNum()) {
                        Cell cell = row.getCell(fromCol);
                        fromValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                    }

                    String toValue = "";
                    if (toCol < row.getLastCellNum()) {
                        Cell cell = row.getCell(toCol);
                        toValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                    }

                    // Check if it's a valid lithology entry (from and to non-empty)
                    if (!fromValue.isEmpty() && !toValue.isEmpty() && !fromValue.equals("nan") && !toValue.equals("nan")) {
                        String intervalValue = "";
                        if (intervalCol != null && intervalCol < row.getLastCellNum()) {
                            Cell cell = row.getCell(intervalCol);
                            intervalValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                        }

                        String wcValue = "";
                        if (wcCol != null && wcCol < row.getLastCellNum()) {
                            Cell cell = row.getCell(wcCol);
                            wcValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                        }

                        String wocValue = "";
                        if (wocCol != null && wocCol < row.getLastCellNum()) {
                            Cell cell = row.getCell(wocCol);
                            wocValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                        }

                        String stageValue = "";
                        if (stageCol != null && stageCol < row.getLastCellNum()) {
                            Cell cell = row.getCell(stageCol);
                            stageValue = (cell != null) ? getCellValueAsString(cell).trim() : "";
                        }

                        // Clean values
                        if (intervalValue.equals("nan")) {
                            intervalValue = "";
                        }
                        if (wcValue.equals("nan")) {
                            wcValue = "";
                        }
                        if (wocValue.equals("nan")) {
                            wocValue = "";
                        }
                        if (stageValue.equals("nan")) {
                            stageValue = "";
                        }

                        JSONObject lithologyEntry = new JSONObject();
                        lithologyEntry.put("from_ft", fromValue);
                        lithologyEntry.put("to_ft", toValue);
                        lithologyEntry.put("total_interval_ft", intervalValue);
                        lithologyEntry.put("wc", wcValue);
                        lithologyEntry.put("woc", wocValue);
                        lithologyEntry.put("stage", stageValue);

                        lithologyData.put(lithologyEntry);
                    } else {
                        // If we encounter an empty row, it's probably the end of lithology data
                        if (fromValue.isEmpty() || fromValue.equals("nan")) {
                            break;
                        }
                    }

                    currentRow++;
                }
            }

            System.out.println("Extracted " + lithologyData.length() + " lithology entries");

        } catch (Exception e) {
            System.err.println("Error parsing lithology data: " + e.getMessage());
            e.printStackTrace();
        }

        return lithologyData;
    }


    /**
     * Method to parse remarks and requirements from Excel sheet.
     * Translated from Python to Java.
     *
     * @param sheet The Excel sheet containing remarks and requirements information
     * @return Object[] containing remarks dictionary (JSONObject) and requirements list (JSONArray)
     */
    public static JSONObject parseRemarksAndRequirements(Sheet sheet) {
        JSONObject result = new JSONObject();
        JSONObject remarksDict = new JSONObject();
        JSONArray requirements = new JSONArray();
        JSONArray remarks = new JSONArray();

        try {
            // Determine the max column index (BG column if it exists)
            int maxColIdx = 54; // Default to column BG (55th column, 0-indexed)

            // Search for the remarks section
            boolean remarksSectionFound = false;
            Integer remarksRow = null;

            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append(" ");
                    }
                }

                String rowText = rowStr.toString().trim();

                if (rowText.contains("REMARKS")) {
                    remarksSectionFound = true;
                    remarksRow = i;
                    break;
                }
            }

            // If the remarks section was found, extract remarks up to row 57
            String plannedOperation = null;
            if (remarksSectionFound && remarksRow != null) {
                int currentRow = remarksRow + 1;
                int endRow = Math.min(lastRow, 57);  // Limit to row 57 (or end of sheet)

                List<String> tempRemarks = new ArrayList<>();  // Temporary storage for all remarks

                while (currentRow <= endRow) {
                    Row rowData = sheet.getRow(currentRow);
                    if (rowData == null) {
                        currentRow++;
                        continue;
                    }

                    // Analyze columns up to maxColIdx
                    for (int colIdx = 0; colIdx <= Math.min(maxColIdx, rowData.getLastCellNum() - 1); colIdx++) {
                        Cell cell = rowData.getCell(colIdx);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        // Exclude empty values and "nan"
                        if (!cellValue.isEmpty() && !cellValue.equals("nan") && !cellValue.toLowerCase().equals("planned operation")) {
                            // Check if it's a significant remark
                            // Exclude cells that contain only numbers or special characters
                            if (!cellValue.matches("^[\\d\\s\\.,\\-/()]+$") && cellValue.length() > 1) {
                                tempRemarks.add(cellValue);
                            }
                        }
                    }

                    currentRow++;
                }

                // Separate normal remarks from planned operation
                if (!tempRemarks.isEmpty()) {
                    // Remove duplicates while preserving order
                    Set<String> seen = new LinkedHashSet<>();
                    List<String> uniqueRemarks = new ArrayList<>();
                    for (String remark : tempRemarks) {
                        if (!seen.contains(remark)) {
                            seen.add(remark);
                            uniqueRemarks.add(remark);
                        }
                    }

                    // The last remark becomes the planned operation
                    if (!uniqueRemarks.isEmpty()) {
                        plannedOperation = uniqueRemarks.get(uniqueRemarks.size() - 1);
                    }

                    // All but the last two remarks go to the remarks list
                    if (uniqueRemarks.size() > 1) {
                        for (int i = 0; i < uniqueRemarks.size() - 2; i++) {
                            remarks.put(uniqueRemarks.get(i));
                        }
                    }
                }
            }

            // Search for the requirements section
            boolean requirementsSectionFound = false;
            Integer requirementsRow = null;

            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        rowStr.append(getCellValueAsString(cell)).append(" ");
                    }
                }

                String rowText = rowStr.toString().trim();

                if (rowText.contains("REQUIREMENTS")) {
                    requirementsSectionFound = true;
                    requirementsRow = i;
                    break;
                }
            }

            // If the requirements section was found, extract requirements up to row 64
            if (requirementsSectionFound && requirementsRow != null) {
                int currentRow = requirementsRow + 1;
                int endRow = Math.min(lastRow, 64);  // Limit to row 64 (or end of sheet)

                List<String> reqList = new ArrayList<>();

                while (currentRow <= endRow) {
                    Row rowData = sheet.getRow(currentRow);
                    if (rowData == null) {
                        currentRow++;
                        continue;
                    }

                    // Analyze columns up to maxColIdx
                    for (int colIdx = 0; colIdx <= Math.min(maxColIdx, rowData.getLastCellNum() - 1); colIdx++) {
                        Cell cell = rowData.getCell(colIdx);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        // Exclude empty values and "nan"
                        if (!cellValue.isEmpty() && !cellValue.equals("nan")) {
                            // Check if it's a significant requirement
                            // Include numbered formats and requirements with "Need"
                            if (cellValue.matches("^\\d+/.*") || cellValue.contains("Need")) {
                                reqList.add(cellValue);
                            }
                            // Include other potential requirement formats
                            else if (cellValue.length() > 5 && !cellValue.matches("^\\d+$")) {
                                reqList.add(cellValue);
                            }
                        }
                    }

                    currentRow++;
                }

                // Remove duplicates while preserving order
                LinkedHashSet<String> uniqueRequirements = new LinkedHashSet<>(reqList);
                for (String req : uniqueRequirements) {
                    requirements.put(req);
                }
            }

            // Create the output structure
            remarksDict.put("remarks", remarks);
            remarksDict.put("Planned_operation", plannedOperation);

            // Add both sections to the main result object
            result.put("remarks", remarksDict);
            result.put("requirements", requirements);

            System.out.println("Extracted " + remarks.length() + " remarks");
            System.out.println("Extracted " + requirements.length() + " requirements");

        } catch (Exception e) {
            System.err.println("Error parsing remarks and requirements: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }



    public static JSONObject parseGlobalInfo(Sheet sheet) {
        JSONObject result = new JSONObject();
        JSONObject safetyMetrics = new JSONObject();
        JSONObject costMetrics = new JSONObject();
        JSONObject personnel = new JSONObject();
        JSONObject drillingEngineer = new JSONObject();
        JSONArray geologist = new JSONArray();
        JSONObject superintendent = new JSONObject();

        try {
            // Initialisation des structures JSON
            safetyMetrics.put("last_bop_test", "");
            safetyMetrics.put("accident_free_days", "");
            safetyMetrics.put("last_safety_meeting", "");
            safetyMetrics.put("planned_day", "");
            safetyMetrics.put("actual_day", "");

            costMetrics.put("daily_cost", "");
            costMetrics.put("cumulative_cost", "");

            drillingEngineer.put("name", "");
            drillingEngineer.put("id", "");
            superintendent.put("name", "");
            superintendent.put("id", "");

            personnel.put("drilling_engineer", drillingEngineer);
            personnel.put("geologist", geologist);
            personnel.put("superintendent", superintendent);

            result.put("safety_metrics", safetyMetrics);
            result.put("cost_metrics", costMetrics);
            result.put("personnel", personnel);

            // --- PARTIE 1: MÉTRIQUES DE SÉCURITÉ (MÉTHODE ORIGINALE) ---

            // Convertir les noms de colonnes Excel en indices numériques
            int colBD = excelColToIndex("BD");
            int colDP = excelColToIndex("DP");
            int colBP = excelColToIndex("BP");

            // Vérifier si les colonnes sont dans les limites de la feuille
            int lastColumnNum = getLastColumnNum(sheet);
            int lastRowNum = sheet.getLastRowNum();

            if (colDP >= lastColumnNum || colBD >= lastColumnNum) {
                System.out.println("La feuille ne contient pas assez de colonnes. Colonnes attendues: BD-DP, trouvées: " + lastColumnNum);
            } else {
                // Vérifier si les lignes sont dans les limites de la feuille
                if (64 >= lastRowNum || 58 >= lastRowNum) {
                    System.out.println("La feuille ne contient pas assez de lignes. Lignes attendues: 58-64, trouvées: " + lastRowNum);
                } else {
                    try {
                        // Extraction des métriques de sécurité (lignes 58-62, colonnes BD-BP)
                        int row = 57;
                        String[] metricNames = {"Last BOP'S TEST :", "Accident Free Days", "Last Safety Meeting", "Planned Day", "Actual Day"};

                        for (String metricName : metricNames) {
                            // Chercher la cellule contenant le nom de la métrique dans la plage BD-BP
                            boolean metricFound = false;
                            for (int col = colBD; col <= colBP; col++) {
                                Row sheetRow = sheet.getRow(row);
                                if (sheetRow == null) continue;

                                Cell cell = sheetRow.getCell(col);
                                if (cell == null) continue;

                                String cellValue = getCellValueAsString(cell).trim();
                                // Supprimer les deux points pour la comparaison
                                String metricNameToCompare = metricName.replace(":", "").trim();
                                if (cellValue.replace(":", "").trim().contains(metricNameToCompare)) {
                                    metricFound = true;
                                    // Chercher la valeur dans les cellules à droite
                                    for (int valueCol = col + 1; valueCol <= colBP + 2; valueCol++) {
                                        Cell valueCell = sheetRow.getCell(valueCol);
                                        if (valueCell == null) continue;

                                        String value = getCellValueAsString(valueCell).trim();
                                        if (!value.isEmpty() && !value.equals("nan") && !value.contains(":")) {
                                            // Traiter la valeur selon le type de métrique
                                            if (metricName.equals("Last BOP'S TEST :")) {
                                                safetyMetrics.put("last_bop_test", value);
                                            } else if (metricName.equals("Accident Free Days")) {
                                                try {
                                                    safetyMetrics.put("accident_free_days", Integer.parseInt(value));
                                                } catch (NumberFormatException e) {
                                                    safetyMetrics.put("accident_free_days", value);
                                                }
                                            } else if (metricName.equals("Last Safety Meeting")) {
                                                safetyMetrics.put("last_safety_meeting", value);
                                            } else if (metricName.equals("Planned Day")) {
                                                safetyMetrics.put("planned_day", value);
                                            } else if (metricName.equals("Actual Day")) {
                                                try {
                                                    safetyMetrics.put("actual_day", Integer.parseInt(value));
                                                } catch (NumberFormatException e) {
                                                    safetyMetrics.put("actual_day", value);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }

                            if (metricFound) {
                                row++; // Passer à la ligne suivante seulement si la métrique a été trouvée
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'extraction des métriques de sécurité: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // --- PARTIE 2: MÉTRIQUES DE COÛTS (MÉTHODE AMÉLIORÉE) ---

            // Définir une plage plus large pour la recherche des coûts
            int startRow = 57;
            int endRow = Math.min(70, sheet.getLastRowNum());
            int colBZ = excelColToIndex("BZ");
            int colCR = excelColToIndex("CR");

            try {
                for (int row = startRow; row <= endRow; row++) {
                    Row sheetRow = sheet.getRow(row);
                    if (sheetRow == null) continue;

                    for (int col = colBZ; col <= colCR; col++) {
                        Cell cell = sheetRow.getCell(col);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        // Détection Daily Cost
                        if (cellValue.contains("Daily Cost:")) {
                            // Recherche de la valeur dans les colonnes suivantes
                            for (int offset = 3; offset < 17; offset++) {
                                Cell valueCell = sheetRow.getCell(col + offset);
                                if (valueCell == null) continue;

                                String value = getCellValueAsString(valueCell).trim();

                                if (value.replace(".", "").matches("\\d+")) { // Vérifie si c'est un nombre
                                    try {
                                        String cleanValue = value.replace(" ", "").replace(",", ".");
                                        costMetrics.put("daily_cost", Double.parseDouble(cleanValue));
                                        break;
                                    } catch (NumberFormatException e) {
                                        costMetrics.put("daily_cost", value);
                                        break;
                                    }
                                }
                            }
                        }
                        // Détection Cumulative Cost
                        else if (cellValue.contains("Cumulative cost:")) {
                            // Recherche de la valeur dans les colonnes suivantes
                            for (int offset = 3; offset < 17; offset++) {
                                Cell valueCell = sheetRow.getCell(col + offset);
                                if (valueCell == null) continue;

                                String value = getCellValueAsString(valueCell).trim();

                                if (value.replace(".", "").replace("US$", "").trim().matches("\\d+")) {
                                    try {
                                        String cleanValue = value.replace("US$", "").replace(" ", "").replace(",", ".");
                                        costMetrics.put("cumulative_cost", Double.parseDouble(cleanValue));
                                        break;
                                    } catch (NumberFormatException e) {
                                        costMetrics.put("cumulative_cost", value);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'extraction des coûts: " + e.getMessage());
                e.printStackTrace();
            }

            // --- PARTIE 3: PERSONNEL (MÉTHODE AMÉLIORÉE) ---

            // Recherche du personnel
            Map<String, String> personnelTitles = new HashMap<>();
            personnelTitles.put("Drilling engineer", "drilling_engineer");
            personnelTitles.put("Geologist", "geologist");
            personnelTitles.put("Superintendent", "superintendent");

            Map<String, int[]> foundTitles = new HashMap<>();

            try {
                // Première passe: trouver les titres de personnel
                for (int row = startRow; row <= endRow; row++) {
                    Row sheetRow = sheet.getRow(row);
                    if (sheetRow == null) continue;

                    for (int col = 0; col < lastColumnNum; col++) {
                        Cell cell = sheetRow.getCell(col);
                        if (cell == null) continue;

                        String cellValue = getCellValueAsString(cell).trim();

                        for (Map.Entry<String, String> entry : personnelTitles.entrySet()) {
                            String titleLabel = entry.getKey();
                            String titleKey = entry.getValue();

                            if (cellValue.contains(titleLabel)) {
                                foundTitles.put(titleKey, new int[]{row, col});
                            }
                        }
                    }
                }

                // Deuxième passe: récupérer les valeurs pour chaque titre
                for (Map.Entry<String, int[]> entry : foundTitles.entrySet()) {
                    String titleKey = entry.getKey();
                    int[] position = entry.getValue();
                    int row = position[0];
                    int col = position[1];

                    // Chercher la valeur dans la cellule en-dessous
                    Row nextRow = sheet.getRow(row + 1);
                    if (nextRow != null) {
                        Cell valueCell = nextRow.getCell(col);
                        if (valueCell != null) {
                            String value = getCellValueAsString(valueCell).trim();

                            if (!value.isEmpty() && !value.equals("nan")) {
                                if (titleKey.equals("geologist")) {
                                    // Traitement spécial pour les géologues (potentiellement multiples)
                                    if (value.contains("//")) {
                                        String[] geologists = value.split("//");
                                        for (String geo : geologists) {
                                            geo = geo.trim();
                                            if (!geo.isEmpty()) {
                                                String[] nameIdPair = extractNameId(geo);
                                                String name = nameIdPair[0];
                                                String id = nameIdPair[1];

                                                if (!name.isEmpty()) {
                                                    JSONObject geologistObj = new JSONObject();
                                                    geologistObj.put("name", name);
                                                    geologistObj.put("id", id);
                                                    geologist.put(geologistObj);
                                                }
                                            }
                                        }
                                    } else {
                                        String[] nameIdPair = extractNameId(value);
                                        String name = nameIdPair[0];
                                        String id = nameIdPair[1];

                                        if (!name.isEmpty()) {
                                            JSONObject geologistObj = new JSONObject();
                                            geologistObj.put("name", name);
                                            geologistObj.put("id", id);
                                            geologist.put(geologistObj);
                                        }
                                    }
                                } else {
                                    String[] nameIdPair = extractNameId(value);
                                    String name = nameIdPair[0];
                                    String id = nameIdPair[1];

                                    JSONObject personObj = (JSONObject) personnel.get(titleKey);
                                    personObj.put("name", name);
                                    personObj.put("id", id);
                                }
                            }
                        }
                    }
                }

                // Chercher à nouveau les titres qui n'ont pas été trouvés
                Map<String, String> reversedTitles = new HashMap<>();
                for (Map.Entry<String, String> entry : personnelTitles.entrySet()) {
                    reversedTitles.put(entry.getValue(), entry.getKey());
                }

                for (Map.Entry<String, String> entry : reversedTitles.entrySet()) {
                    String titleKey = entry.getKey();
                    String titleLabel = entry.getValue();

                    // Chercher seulement les titres qui n'ont pas encore été trouvés
                    if (!foundTitles.containsKey(titleKey)) {
                        for (int row = startRow; row <= endRow; row++) {
                            Row sheetRow = sheet.getRow(row);
                            if (sheetRow == null) continue;

                            for (int col = 0; col < lastColumnNum; col++) {
                                Cell cell = sheetRow.getCell(col);
                                if (cell == null) continue;

                                String cellValue = getCellValueAsString(cell).trim();

                                // Vérifier sans les deux points
                                if (cellValue.replace(":", "").trim().contains(titleLabel.replace(":", "").trim())) {
                                    // Chercher la valeur dans la cellule en-dessous
                                    Row nextRow = sheet.getRow(row + 1);
                                    if (nextRow != null) {
                                        Cell valueCell = nextRow.getCell(col);
                                        if (valueCell != null) {
                                            String value = getCellValueAsString(valueCell).trim();

                                            if (!value.isEmpty() && !value.equals("nan")) {
                                                if (titleKey.equals("geologist")) {
                                                    // Traitement spécial pour les géologues
                                                    if (value.contains("//")) {
                                                        String[] geologists = value.split("//");
                                                        for (String geo : geologists) {
                                                            geo = geo.trim();
                                                            if (!geo.isEmpty()) {
                                                                String[] nameIdPair = extractNameId(geo);
                                                                String name = nameIdPair[0];
                                                                String id = nameIdPair[1];

                                                                if (!name.isEmpty()) {
                                                                    JSONObject geologistObj = new JSONObject();
                                                                    geologistObj.put("name", name);
                                                                    geologistObj.put("id", id);
                                                                    geologist.put(geologistObj);
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        String[] nameIdPair = extractNameId(value);
                                                        String name = nameIdPair[0];
                                                        String id = nameIdPair[1];

                                                        if (!name.isEmpty()) {
                                                            JSONObject geologistObj = new JSONObject();
                                                            geologistObj.put("name", name);
                                                            geologistObj.put("id", id);
                                                            geologist.put(geologistObj);
                                                        }
                                                    }
                                                } else {
                                                    String[] nameIdPair = extractNameId(value);
                                                    String name = nameIdPair[0];
                                                    String id = nameIdPair[1];

                                                    JSONObject personObj = (JSONObject) personnel.get(titleKey);
                                                    personObj.put("name", name);
                                                    personObj.put("id", id);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Erreur lors de l'extraction du personnel: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Erreur globale dans parseGlobalInfo: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Convertit une référence de colonne Excel (comme "A", "BC") en indice numérique (0-based)
     */
    private static int excelColToIndex(String colRef) {
        int result = 0;
        for (int i = 0; i < colRef.length(); i++) {
            result = result * 26 + (colRef.charAt(i) - 'A' + 1);
        }
        return result - 1; // Convert to 0-based index
    }
    /**
     * Extrait le nom et l'identifiant à partir d'une chaîne
     * @return un tableau de taille 2 contenant [nom, id]
     */
    private static String[] extractNameId(String input) {
        String name = input.trim();
        String id = "";

        // Chercher un motif comme "Nom (ID12345)" ou "Nom ID12345"
        int openParen = input.indexOf('(');
        if (openParen > 0) {
            int closeParen = input.indexOf(')', openParen);
            if (closeParen > openParen) {
                name = input.substring(0, openParen).trim();
                id = input.substring(openParen + 1, closeParen).trim();
            }
        } else {
            // Chercher un espace séparant le nom et l'ID (supposant que l'ID contient des chiffres)
            String[] parts = input.trim().split("\\s+");
            if (parts.length > 1) {
                String lastPart = parts[parts.length - 1];

                // Vérifier si la dernière partie ressemble à un ID (contient des chiffres)
                if (lastPart.matches(".*\\d+.*")) {
                    id = lastPart;
                    name = input.substring(0, input.lastIndexOf(lastPart)).trim();
                }
            }
        }

        // Utiliser regex pour supprimer tous les deux points et espaces à la fin du nom
        // Cette méthode est plus robuste et gère tous les cas possibles
        name = name.replaceAll("\\s*:+\\s*$", "");

        return new String[]{name, id};
    }



    /**
     * Obtient le numéro de la dernière colonne de la feuille
     */
    private static int getLastColumnNum(Sheet sheet) {
        int lastColumnNum = 0;
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            int lastCellNum = row.getLastCellNum();

            if (lastCellNum > lastColumnNum) {
                lastColumnNum = lastCellNum;
            }
        }

        return lastColumnNum;
    }





    /**
     * Updated parseDrillingReport method to include parameters extraction
     */
    public static JSONObject parseDrillingReport(String filePath, String jsonOutputPath) {
        FileInputStream file = null;
        Workbook workbook = null;
        JSONObject drillingData = null;

        try {
            System.out.println("Opening file: " + filePath);
            file = new FileInputStream(new File(filePath));
            workbook = WorkbookFactory.create(file);

            Sheet sheet = workbook.getSheetAt(0); // First sheet for the drilling report
            System.out.println("Sheet opened: " + sheet.getSheetName());

            // Parse header data
            JSONObject headerData = parseDrillingReportHeader(sheet);
            System.out.println("Parsed header data: " + headerData.toString(2));

            // Parse drilling parameters
            JSONObject parametersData = parseDrillingParameters(sheet);
            System.out.println("Parsed parameters data: " + parametersData.toString(2));

            // Parse mud information
            JSONObject mudInformationData = parseMudInformation(sheet);
            System.out.println("Parsed mud information data: " + mudInformationData.toString(2));

            // Parse operations
            JSONObject operationData = parseOperations(sheet);
            System.out.println("Parsed operation data: " + operationData.toString(2));

            // Parse BHA components
            JSONObject bhaData = parseBHAReport(sheet);
            System.out.println("Parsed BHA data: " + bhaData.toString(2));

            // Parse mud products
            JSONObject mudProductData = parseBHAReport(sheet);
            System.out.println("Parsed mud products data: " + mudProductData.toString(2));

            // Parse Lithology
            JSONArray lithologyData = parseLithology(sheet);
            System.out.println("Parsed lithology data: " + lithologyData.toString(2));

            // Parse Remarks
            JSONObject remarksData = parseRemarksAndRequirements(sheet);
            System.out.println("Parsed remarks data: " + remarksData.toString(2));

            // Parse Global Info
            JSONObject globalInfoData = parseGlobalInfo(sheet);
            System.out.println("Parsed global info data: " + globalInfoData.toString(2));

            // Combine all into one JSON object
            Map<String, Object> orderedMap = new LinkedHashMap<>();
            orderedMap.put("header", headerData);
            orderedMap.put("parameters", parametersData);
            orderedMap.put("mud_information", mudInformationData);
            orderedMap.put("operations", operationData);
            orderedMap.put("bha_components", bhaData);
            orderedMap.put("mud_products", mudProductData);
            orderedMap.put("lithology", lithologyData);
            orderedMap.put("remarks", remarksData);
            orderedMap.put("global_info", globalInfoData);

            drillingData = new JSONObject(orderedMap);

            // Save to JSON file if path is provided
            if (jsonOutputPath != null && !jsonOutputPath.isEmpty()) {
                try {
                    // Use relative path from the project root
                    String outputDirectory = "C:\\Users\\dinap\\OneDrive\\Bureau\\ESI\\2CS\\S2\\Projet 2CS\\test\\test_j2ee\\src\\main\\webapp\\WEB-INF\\data";

                    // Extract just the filename from the jsonOutputPath
                    String fileName = new File(jsonOutputPath).getName();

                    // Create the output directory if it doesn't exist
                    File dataDir = new File(outputDirectory);
                    if (!dataDir.exists()) {
                        dataDir.mkdirs(); // Using mkdirs() to create parent directories if needed
                    }

                    // Build the path to the file in the specified directory
                    File jsonFile = new File(dataDir, fileName);

                    // Write the JSON file
                    try (java.io.FileWriter writer = new java.io.FileWriter(jsonFile)) {
                        writer.write(drillingData.toString(2));
                        System.out.println("JSON data written to: " + jsonFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.err.println("Error writing JSON to file: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            return drillingData;
        } catch (Exception e) {
            System.err.println("Error parsing drilling report: " + e.getMessage());
            e.printStackTrace();
            // Return at least an empty JSON object rather than null
            return new JSONObject();
        } finally {
            try {
                if (workbook != null) workbook.close();
                if (file != null) file.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }


    public static JSONObject importDrillingReport(String filePath, FichierDrillingDAO fichierDrillingDAO) {
        try {
            // 1. Génère le JSON à partir du rapport (et l’enregistre dans data/)
            JSONObject drillingData = parseDrillingReport(filePath, "drilling_report_data.json");

            // 2. Lit le fichier Excel en binaire
            byte[] excelBytes = Files.readAllBytes(Paths.get(filePath));

            // 3. Crée et enregistre l’objet
            FICHIER_DRILLING fichier = new FICHIER_DRILLING();
            fichier.setNomFichier(new File(filePath).getName());
            fichier.setContenuFichier(excelBytes);
            fichier.setJsonData(drillingData.toString(2));
            fichier.setDateUpload(new Date());

            fichierDrillingDAO.save(fichier);
            System.out.println("✔️ Rapport et JSON enregistrés avec succès en base.");

            return drillingData;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'import du fichier :");
            e.printStackTrace();
            return null; // ou throw RuntimeException(e) pour forcer la gestion côté servlet
        }
    }


}

