package jatx.reflectdata.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import jatx.reflectdata.annotations.DoNotCreateNorInsert;
import jatx.reflectdata.annotations.DoNotInsert;

public class QueryGenerator {
    private String _tableName;
    private Class clazz;
    private List<Object> defaultValueList;
    private int dbVersion;
    private boolean ifNotExists;

    private Map<String, String> functionValueMap = new HashMap<>();
    public static final int INSERT_DEFAULT = 0;
    public static final int INSERT_REPLACE = 1;
    public static final int INSERT_IGNORE = 2;
    
    private int insertType;

    private Map<Column, Object> valueMap = new HashMap();
    private Map<Column, Integer> indexMap = new HashMap();

    private int initIndex = 0;
    
    public QueryGenerator(String _tableName, Class clazz, int dbVersion, boolean ifNotExists) {
        super();
        this._tableName = _tableName;
        this.clazz = clazz;
        this.dbVersion = dbVersion;
        this.ifNotExists = ifNotExists;
        defaultValueList = new ArrayList<Object>();
        insertType = INSERT_DEFAULT;
    }

    public String generateCreateTableQuery() throws ReflectSQLExceptions.MoreThanOnePrimaryKeyException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<Column> columnList = new ArrayList();
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers)) && (!field.isAnnotationPresent(DoNotCreateNorInsert.class))) {
                Column column = Column.fromField(field);
                if ((column != null) && (dbVersion >= column.fromVersion)) {
                    columnList.add(column);
                    if (column.hasDefaultValue) {
                        defaultValueList.add(column.getDefaultValue());
                    }
                }
            }
        }

        String header = Const.STRING_CREATE_TABLE + (ifNotExists ? Const.STRING_IF_NOT_EXISTS : "") +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + " (\n";

        int primaryKeyCount = 0;
        String primaryKeyName = "";

        List<String> columnStringList = new ArrayList();

        for (Column column : columnList) {
            if ((column.isPrimaryKey) || (column.isAutoIncrement)) {
                primaryKeyCount++;
                primaryKeyName = column.name;
            }
            columnStringList.add(column.toString());
        }

        if (primaryKeyCount > 1) throw new ReflectSQLExceptions.MoreThanOnePrimaryKeyException();
        String footer = "";
        if (primaryKeyCount == 1) {
            footer = Const.STRING_PRIMARY_KEY + Const.STRING_BEFORE_NAME + primaryKeyName + Const.STRING_AFTER_NAME + "))";
        } else {
            footer = ")";
        }

        return header + StringUtils.join(columnStringList, ",\n") + footer + Const.STRING_QUERY_ENDING;
    }

    public String generateInsertQuery(Object object) throws IllegalAccessException, ReflectSQLExceptions.NotNullColumnHasNullValueException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;
        List<String> setValueStringList = new ArrayList();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers))
                    && (!field.isAnnotationPresent(DoNotInsert.class))
                    && (!field.isAnnotationPresent(DoNotCreateNorInsert.class))) {
                Column column = Column.fromField(field);
                if ((column != null) && (!(column.isAutoIncrement && insertType==INSERT_DEFAULT))  && (dbVersion >= column.fromVersion)) {
                    if (functionValueMap.containsKey(column.name)) {
                        String setValueString = "`" + column.name + "`" + "=" + (String)functionValueMap.get(column.name);
                        setValueStringList.add(setValueString);
                    } else {
                        Object value = null;
                        value = field.get(object);
                        if ((value != null) || (!column.hasDefaultValue)) {
                            if ((column.isNotNull) && (value == null)) {
                                throw new ReflectSQLExceptions.NotNullColumnHasNullValueException();
                            }
                            String setValueString = Const.STRING_BEFORE_NAME + column.name + Const.STRING_AFTER_NAME
                                    + Const.STRING_ASSIGN + (value != null ? Const.STRING_VAR : Const.STRING_NULL);
                            setValueStringList.add(setValueString);
                            if (value != null) {
                                if ((value.getClass() == Character.TYPE) || (value.getClass() == Character.class)) {
                                    valueMap.put(column, String.valueOf(value));
                                } else {
                                    valueMap.put(column, value);
                                }
                                indexMap.put(column, Integer.valueOf(++initIndex));
                            }
                        }
                    }
                }
            }
        }
        boolean insertReplaceSet = (insertType == INSERT_REPLACE);

        boolean insertIgnoreSet = (insertType == INSERT_IGNORE);

//        String header = (insertReplaceSet ? Const.STRING_REPLACE : Const.STRING_INSERT) +
//                (insertIgnoreSet ? Const.STRING_IGNORE : "") + Const.STRING_INTO +
//                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + Const.STRING_SET;

        String header = (insertReplaceSet ? Const.STRING_REPLACE : Const.STRING_INSERT) +
                (insertIgnoreSet ? Const.STRING_IGNORE : "") + Const.STRING_INTO +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + " VALUES(";


        //String result = header + StringUtils.join(setValueStringList, Const.STRING_GLUE) + Const.STRING_QUERY_ENDING;
        String result = header + StringUtils.join(setValueStringList, Const.STRING_GLUE) + ")" + Const.STRING_QUERY_ENDING;
        //System.out.println(result);
        return result;
    }
    
    public String generateInsertCode(Object object) throws IllegalAccessException, ReflectSQLExceptions.NotNullColumnHasNullValueException {
        String query = generateInsertQuery(object);
        String className = clazz.getSimpleName();
        String objectName = com.willdom.util.strings.StringUtils.lowerCaseFirst(className);
        String output = "public void insert" + className + "(Connection connection, " + clazz.getName();
        output += " " + objectName + ") throws SQLException {";
        output += "PreparedStatement statement = null;\n";
        output += "String sql = \"" + query + "\";\n";
        output += "statement = connection.prepareStatement(sql);\n";
        output += "int i = 1;\n";
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String methodName = "get" + com.willdom.util.strings.StringUtils.capitalizeFirst(field.getName())+ "()";
            output+="setLimitedString(statement, i++," + objectName + "." + methodName + ", 100);\n";
        }
        
        output += "     int result = statement.executeUpdate();\r\n" + 
                "        if (result == 0) {\r\n" + 
                "            logger.warn(\"" + className + " could not be added\");\r\n" + 
                "        }\r\n" + 
                "        statement.close();\n"
                + "}";

        return output;
    }
    
    public int getInsertType() {
        return insertType;
    }
    
    public void setInsertType(int insertType) {
        this.insertType = insertType;
    }


}
