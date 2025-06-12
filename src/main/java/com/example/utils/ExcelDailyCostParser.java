package com.example.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExcelDailyCostParser {

    private static final String[] SPECIAL_ITEMS = {"GR-RESISTIVITY-CALIPER"};

    public static JSONObject extractDailyCostData(String excelFilePath, String outputJsonPath) {
        try {
            // Vérifier si le fichier existe
            if (!Files.exists(Paths.get(excelFilePath))) {
                System.err.println("Le fichier " + excelFilePath + " n'existe pas.");
                return null;
            }

            // Charger le fichier Excel
            FileInputStream file = new FileInputStream(new File(excelFilePath));
            Workbook workbook = new XSSFWorkbook(file);

            // Trouver la feuille appropriée
            Sheet sheet = workbook.getSheet("Daily Cost");
            if (sheet == null) {
                if (workbook.getNumberOfSheets() >= 2) {
                    sheet = workbook.getSheetAt(1);
                } else {
                    sheet = workbook.getSheetAt(0);
                }
            }

            // ... (tout le code de traitement reste identique) ...

            // Initialisation des structures de données
            Map<String, JSONObject> mainCategories = new LinkedHashMap<>();
            List<String> categoryOrder = new ArrayList<>();
            String currentCategory = null;
            Object dailyCostTotal = null;

            // Parcourir les lignes de la feuille
            for (Row row : sheet) {
                try {
                    Cell descriptionCell = row.getCell(0);
                    String description = descriptionCell != null ? getCellValueAsString(descriptionCell) : "";

                    if (description.trim().isEmpty()) {
                        continue;
                    }

                    // Vérifier le total "Daily cost" global
                    if (description.trim().equalsIgnoreCase("daily cost") && row.getLastCellNum() > 6) {
                        Cell valueCell = row.getCell(6);
                        if (valueCell != null) {
                            Object value = getCellValue(valueCell);
                            if (isNumericAndLarge(value)) {
                                dailyCostTotal = value;
                                continue;
                            }
                        }
                    }

                    // Vérifier si c'est une catégorie principale
                    boolean isSpecialItem = false;
                    for (String item : SPECIAL_ITEMS) {
                        if (description.trim().equals(item)) {
                            isSpecialItem = true;
                            break;
                        }
                    }

                    if ((description.trim().equals(description.trim().toUpperCase()) || description.contains("TOTAL"))
                            && !isSpecialItem) {

                        if (description.contains("TOTAL") && currentCategory != null) {
                            // Ligne de total pour la catégorie actuelle
                            Cell totalCell = row.getCell(6);
                            if (totalCell != null && !getCellValueAsString(totalCell).isEmpty()) {
                                Object totalValue = getCellValue(totalCell);
                                if (mainCategories.containsKey(currentCategory)) {
                                    mainCategories.get(currentCategory).put("total", formatNumber(totalValue));
                                }
                            }
                        } else if (!description.startsWith("TOTAL")) {
                            // Nouvelle catégorie principale
                            currentCategory = description.trim();
                            if (!mainCategories.containsKey(currentCategory)) {
                                JSONObject category = new JSONObject();
                                category.put("items", new JSONArray());
                                category.put("total", "");
                                mainCategories.put(currentCategory, category);
                                categoryOrder.add(currentCategory);
                            }
                        }
                    } else {
                        // Item de catégorie ou élément spécial
                        if (currentCategory != null || isSpecialItem) {
                            // Traitement spécial pour GR-RESISTIVITY-CALIPER
                            if (isSpecialItem && currentCategory == null) {
                                currentCategory = "ELECTRIC LOGGING SERVICES";
                                if (!mainCategories.containsKey(currentCategory)) {
                                    JSONObject category = new JSONObject();
                                    category.put("items", new JSONArray());
                                    category.put("total", "");
                                    mainCategories.put(currentCategory, category);
                                    categoryOrder.add(currentCategory);
                                }
                            }

                            // Créer l'objet item avec l'ordre des champs comme en Python
                            JSONObject item = createOrderedItem(description, row);

                            // Ajouter l'item à la catégorie
                            mainCategories.get(currentCategory).getJSONArray("items").put(item);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du traitement d'une ligne: " + e.getMessage());
                    continue;
                }
            }

            // Nettoyer les catégories mal classées (GR-RESISTIVITY-CALIPER)
            if (mainCategories.containsKey("GR-RESISTIVITY-CALIPER")) {
                JSONObject grItem = new JSONObject();
                grItem.put("description", "GR-RESISTIVITY-CALIPER");
                grItem.put("lump_sum_day_rate", mainCategories.get("GR-RESISTIVITY-CALIPER").opt("total"));
                grItem.put("unit", "$");
                grItem.put("discount", "");
                grItem.put("quantity", "");
                grItem.put("daily_cost", "");

                String targetCategory = "ELECTRIC LOGGING SERVICES";
                if (!mainCategories.containsKey(targetCategory)) {
                    JSONObject category = new JSONObject();
                    category.put("items", new JSONArray());
                    category.put("total", "");
                    mainCategories.put(targetCategory, category);
                    categoryOrder.add(targetCategory);
                }

                mainCategories.get(targetCategory).getJSONArray("items").put(grItem);
                mainCategories.remove("GR-RESISTIVITY-CALIPER");
                categoryOrder.remove("GR-RESISTIVITY-CALIPER");
            }

            // Utiliser un JSONObject personnalisé qui préserve l'ordre d'insertion
            CustomJSONObject dailyCost = new CustomJSONObject();

            // Ajouter les catégories dans l'ordre d'origine
            for (String categoryName : categoryOrder) {
                JSONObject category = mainCategories.get(categoryName);

                // Créer une catégorie avec l'ordre des champs comme en Python
                CustomJSONObject orderedCategory = new CustomJSONObject();
                orderedCategory.put("items", category.getJSONArray("items"));
                orderedCategory.put("total", category.opt("total"));

                dailyCost.put(categoryName, orderedCategory);
            }

            // Ajouter le total daily_cost si trouvé
            if (dailyCostTotal != null) {
                dailyCost.put("daily_cost_total", dailyCostTotal);
            }

            // Créer l'objet final avec l'ordre voulu
            CustomJSONObject data = new CustomJSONObject();
            data.put("daily_cost", dailyCost);

            // Sauvegarder dans un fichier JSON avec le chemin webapp correct
            try {
                // Obtenir le chemin réel du répertoire WEB-INF/data
                String outputDirectory = "C:\\Users\\dinap\\OneDrive\\Bureau\\ESI\\2CS\\S2\\Projet 2CS\\test\\test_j2ee\\src\\main\\webapp\\WEB-INF\\data";
                String fileName = "daily_cost_data.json";

                // Create the output directory if it doesn't exist
                File dataDir = new File(outputDirectory);
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }

                // Build the path to the file in the specified directory
                File jsonFile = new File(dataDir, fileName);

                // Write the JSON file
                try (FileWriter writer = new FileWriter(jsonFile)) {
                    writer.write(data.toString(4));
                    System.out.println("Daily cost data JSON written to: " + jsonFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("Error writing daily cost JSON to file: " + e.getMessage());
                e.printStackTrace();
            }

            workbook.close();
            file.close();

            return data;

        } catch (Exception e) {
            System.err.println("Une erreur s'est produite: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject createOrderedItem(String description, Row row) {
        // Utiliser un JSONObject personnalisé qui préserve l'ordre d'insertion
        CustomJSONObject item = new CustomJSONObject();

        // Insérer les propriétés dans le même ordre que la version Python
        item.put("description", description);

        // Lump sum day rate (colonne 1)
        Cell lumpSumCell = row.getCell(1);
        Object lumpSumValue = lumpSumCell != null ? getCellValue(lumpSumCell) : "";
        item.put("lump_sum_day_rate", formatNumber(lumpSumValue));

        // Unit (colonne 2)
        item.put("unit", row.getCell(2) != null ? getCellValueAsString(row.getCell(2)) : "");

        // Discount (colonne 3)
        item.put("discount", row.getCell(3) != null ? getCellValueAsString(row.getCell(3)) : "");

        // Quantity (colonne 5)
        item.put("quantity", row.getCell(5) != null ? getCellValueAsString(row.getCell(5)) : "");

        // Daily cost (colonne 6)
        Cell dailyCostCell = row.getCell(6);
        Object dailyCostValue = dailyCostCell != null ? getCellValue(dailyCostCell) : "";
        item.put("daily_cost", formatNumber(dailyCostValue));

        return item;
    }

    private static String formatNumber(Object value) {
        if (value == null) return "";

        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            if (num == (long) num) {
                return String.valueOf((long) num);
            } else {
                return String.valueOf(num);
            }
        } else if (value instanceof String) {
            try {
                // Essayer de parser pour uniformiser le format
                double num = Double.parseDouble(((String) value).replace(",", ""));
                if (num == (long) num) {
                    return String.valueOf((long) num);
                } else {
                    return String.valueOf(num);
                }
            } catch (NumberFormatException e) {
                return value.toString();
            }
        }
        return value.toString();
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
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

    private static Object getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return (long) numValue;
                    }
                    return numValue;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case BLANK:
                return "";
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
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

    private static boolean isNumericAndLarge(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue() > 70000;
        } else if (value instanceof String) {
            try {
                String strValue = ((String) value).replace(",", "");
                double numValue = Double.parseDouble(strValue);
                return numValue > 70000;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Classe personnalisée qui étend JSONObject pour préserver l'ordre d'insertion des clés
     */
    static class CustomJSONObject extends JSONObject {
        private final List<String> keyOrder = new ArrayList<>();

        @Override
        public JSONObject put(String key, Object value) {
            if (!this.has(key)) {
                keyOrder.add(key);
            }
            return super.put(key, value);
        }

        @Override
        public String toString(int indentFactor) {
            try {
                Writer writer = new StringWriter();
                this.customWrite(writer, indentFactor, 0);
                return writer.toString();
            } catch (Exception e) {
                return super.toString(indentFactor);
            }
        }

        // Renommé en customWrite pour éviter le conflit
        private void customWrite(Writer writer, int indentFactor, int indent) throws IOException {
            writer.write("{");

            if (keyOrder.size() > 0) {
                writer.write("\n");
            }

            for (int i = 0; i < keyOrder.size(); i++) {
                String key = keyOrder.get(i);
                Object value = this.opt(key);
                if (value != null) {
                    // Indentation
                    indent(writer, indent + indentFactor);

                    writer.write('"');
                    writer.write(escape(key));
                    writer.write("\": ");

                    if (value instanceof JSONObject) {
                        ((CustomJSONObject)value).customWrite(writer, indentFactor, indent + indentFactor);
                    } else if (value instanceof JSONArray) {
                        ((JSONArray)value).write(writer, indentFactor, indent + indentFactor);
                    } else if (value instanceof String) {
                        writer.write('"');
                        writer.write(escape((String)value));
                        writer.write('"');
                    } else {
                        writer.write(value.toString());
                    }

                    if (i < keyOrder.size() - 1) {
                        writer.write(',');
                    }

                    // Ajout du retour à la ligne supplémentaire
                    writer.write("\n");

                    // Ajout de l'indentation pour la ligne vide
                    indent(writer, indent + indentFactor);
                    writer.write("\n");
                }
            }

            if (keyOrder.size() > 0) {
                indent(writer, indent);
            }
            writer.write("}");
        }

        private void indent(Writer writer, int count) throws IOException {
            for (int i = 0; i < count; i++) {
                writer.write(' ');
            }
        }

        private String escape(String string) {
            if (string == null) {
                return null;
            }
            StringWriter writer = new StringWriter();
            try {
                escape(writer, string);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return writer.toString();
        }

        private void escape(Writer writer, String string) throws IOException {
            for (int i = 0; i < string.length(); i++) {
                char ch = string.charAt(i);
                switch (ch) {
                    case '"':
                        writer.write("\\\"");
                        break;
                    case '\\':
                        writer.write("\\\\");
                        break;
                    case '\b':
                        writer.write("\\b");
                        break;
                    case '\f':
                        writer.write("\\f");
                        break;
                    case '\n':
                        writer.write("\\n");
                        break;
                    case '\r':
                        writer.write("\\r");
                        break;
                    case '\t':
                        writer.write("\\t");
                        break;
                    default:
                        if (ch <= '\u001F') {
                            writer.write(String.format("\\u%04x", (int) ch));
                        } else {
                            writer.write(ch);
                        }
                }
            }
        }
    }
}