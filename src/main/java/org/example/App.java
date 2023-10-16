package org.example;

import entity.DestinationCommonFormat;
import entity.DestinationDataEntity;
import mapper.SqlMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class App {

    private final SqlMapper sqlMapper;

    public App(SqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
    }

    public static void main(String[] args) {
        //copyFieldsIntoDestinationData();
        System.out.println("success");
    }

    private List<DestinationDataEntity>  copyFieldsIntoDestinationData(Object source, DestinationDataEntity destination, String tableName, Integer fileIdentifierId) throws IllegalAccessException {
        Class<?> sourceClass = source.getClass();
        Field[] sourceFields = sourceClass.getDeclaredFields();

        // new return or use paramater
        List<DestinationDataEntity> newDataList = new ArrayList<>();

        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);

            List<String> fieldNamesDestinationCommonFormat = getFieldNames(DestinationCommonFormat.class);
            List<String> fieldNamesDestinationData = getFieldNames(DestinationDataEntity.class);

            List<String> fieldValues = new ArrayList<>();

            Map<String, String> fieldNameToValueMap = new HashMap<>();

            for (String fieldName : fieldNamesDestinationCommonFormat) {

                if (fieldNamesDestinationData.contains(fieldName) && (!fieldName.equals("createUserId")) && (!fieldName.equals("createdDatetime"))) {

                    System.out.println("Processing field: " + fieldName);
                    String fieldValue = getFieldValue(source, fieldName);

                    fieldValues.add(fieldValue);
                    // make up maps
                    fieldNameToValueMap.put(fieldName, fieldValue);
                }
            }

            for (Map.Entry<String, String> entry : fieldNameToValueMap.entrySet()) {

                String fieldName = entry.getKey();
                String replacementValue = entry.getValue();

                // AS modified_column ***
                if (replacementValue != null) {
                    String sqlQuery = "SELECT " + replacementValue + " AS modified_column FROM " + tableName + " where id = " + fileIdentifierId;

                    Map<String, String> row = sqlMapper.selectOne(sqlQuery);
                    for (String key : row.keySet()) {
                        System.out.println("Key: " + key);
                    }
                    String modifiedColumnValue = (String) row.get("modified_column");

                    // set value into  list by refliction
                    Class<?> dataClass = destination.getClass();

                    try {
                        Field field = dataClass.getDeclaredField(fieldName);
                        field.setAccessible(true);

                        Class<?> fieldType = field.getType();

                        // TODO !!!!! Other types
                        if (fieldType == Integer.class || fieldType == int.class) {
                            int intValue = Integer.parseInt(modifiedColumnValue);
                            field.set(destination, intValue);
                        } else if (fieldType == Byte.class) {
                            byte byteValue = Byte.parseByte(modifiedColumnValue);
                            field.set(destination, byteValue);
                        } else if (fieldType == String.class) {
                            field.set(destination, modifiedColumnValue);
                        } else {

                        }
                    } catch (NoSuchFieldException | IllegalAccessException | NumberFormatException e) {
                        e.printStackTrace();
                    }
                    newDataList.add(destination);


                }

            }

        }
        return newDataList;
    }


    public static List<String> getFieldNames(Class<?> clazz) {
        List<String> fieldNames = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }

    private String getFieldValue(Object source, String fieldName) {
        List<String> fieldValues = new ArrayList<>();

        Class<?> sourceClass = source.getClass();

        try {
            Field field = sourceClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            Object value = field.get(source);
            if (value != null) {
                fieldValues.add(value.toString());
                return value.toString();
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            // Handle exceptions as needed
            return null;
        }
        return null;

    }
}
