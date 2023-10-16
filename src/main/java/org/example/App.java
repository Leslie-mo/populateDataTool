package org.example;

import entity.BillingCommonFormat;
import entity.BillingDataSecond;
import mapper.SqlMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{

    private final SqlMapper sqlMapper;

    public App(SqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
    }
    public static void main( String[] args )
    {
        //copyFieldsIntoBillingDataSecond();
        System.out.println( "success" );
    }
    private void copyFieldsIntoBillingDataSecond(Object source, BillingDataSecond destinationSecond, String tableName,
                                                 Integer fileIdentifierId) throws IllegalAccessException {
        Class<?> sourceClass = source.getClass();
        Field[] sourceFields = sourceClass.getDeclaredFields();

        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);


            List<String> fieldNamesBillingCommonFormat = getFieldNames(BillingCommonFormat.class);
            List<String> fieldNamesBillingDataSecond = getFieldNames(BillingDataSecond.class);

            List<String> fieldValues = new ArrayList<>();

            Map<String, String> fieldNameToValueMap = new HashMap<>();


            for (String fieldName : fieldNamesBillingCommonFormat) {

                if (fieldNamesBillingDataSecond.contains(fieldName) && (!fieldName.equals("createUserId"))
                        && (!fieldName.equals("createdDatetime"))) {

                    System.out.println("Processing field: " + fieldName);
                    String fieldValue = getFieldValue(source, fieldName);

                    fieldValues.add(fieldValue);
                    // make up maps
                    fieldNameToValueMap.put(fieldName, fieldValue);
                }
            }

            // new return or use paramater
            List<BillingDataSecond> newDataSecondList = new ArrayList<>();

            for (Map.Entry<String, String> entry : fieldNameToValueMap.entrySet()) {

                String fieldName = entry.getKey();
                String replacementValue = entry.getValue();

                // AS modified_column ***
                if (replacementValue != null) {
                    String sqlQuery = "SELECT " + replacementValue + " AS modified_column FROM " + tableName
                            + " where id = " + fileIdentifierId;

                    Map<String, String> row = sqlMapper.selectOne(sqlQuery);


                    for (String key : row.keySet()) {
                        System.out.println("Key: " + key);
                    }
                    String modifiedColumnValue = (String) row.get("modified_column");

                    // set value into second list by refliction
                    Class<?> dataSecondClass = destinationSecond.getClass();

                    try {
                        Field field = dataSecondClass.getDeclaredField(fieldName);
                        field.setAccessible(true);

                        Class<?> fieldType = field.getType();

                        // TODO !!!!! Other types
                        if (fieldType == Integer.class || fieldType == int.class) {
                            int intValue = Integer.parseInt(modifiedColumnValue);
                            field.set(destinationSecond, intValue);
                        } else if (fieldType == Byte.class) {
                            byte byteValue = Byte.parseByte(modifiedColumnValue);
                            field.set(destinationSecond, byteValue);
                        } else if (fieldType == String.class) {
                            field.set(destinationSecond, modifiedColumnValue);
                        } else {
                            // TODO others type
                        }
                    } catch (NoSuchFieldException | IllegalAccessException | NumberFormatException e) {
                        e.printStackTrace();
                    }

                    newDataSecondList.add(destinationSecond);


                } else {
                    // TODO when not map in second list
                }

            }

        }
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
