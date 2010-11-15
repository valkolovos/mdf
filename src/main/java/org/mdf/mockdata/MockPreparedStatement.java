package org.mdf.mockdata;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.mdf.mockdata.generated.Test;

import com.mockrunner.mock.jdbc.MockArray;

public class MockPreparedStatement extends
		com.mockrunner.mock.jdbc.MockPreparedStatement {

	private static Pattern _joinPattern = Pattern
			.compile("(?>(?>(?>cross +|(?>natural +)?inner +|(?>natural +)?(?>left|right|full) +outer +)?)join) +(\\w+)");
	private static Pattern _insertPattern1 = Pattern.compile("insert +into +([^ ]+) +\\(([^)]+)\\) +values +\\(([^)]+)\\).*", Pattern.CASE_INSENSITIVE);
	private static Pattern _insertPattern2 = Pattern.compile("insert +into +([^ ]+) +values +\\(([^)]+)\\).*", Pattern.CASE_INSENSITIVE);
	private static Pattern _updatePattern = Pattern.compile("update +([^ ]+) +set +(.*)", Pattern.CASE_INSENSITIVE);
	private static Pattern _functionPattern = Pattern.compile("\\[FN(\\d+)\\]");
	private static Pattern _fromReplacePattern = Pattern.compile("from ");
	private static Pattern _selectReplacePattern = Pattern.compile("select ");
	private static Pattern _multiSpaceReplacePattern = Pattern.compile(" {2,}");
	private static Pattern _singleSpaceReplacePattern = Pattern.compile(" ");
	private static Pattern _tickReplacePattern = Pattern.compile("'");
	private static Pattern _quoteReplacePattern = Pattern.compile("\"");
	private static Pattern _sqlParamReplacePattern = Pattern.compile("\\?");
	protected String _sql;
	protected MockDataManager _dataManager;

	public MockPreparedStatement(Connection connection, String sql,
			MockDataManager dataManager) {
		super(connection, sql);
		_sql = sql;
		_dataManager = dataManager;
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		_sql = sql;
		return executeQuery();
	}

	public String getSQL() {
		return _sql;
	}

	@Override
    public void setBytes(int parameterIndex, byte[] byteArray) throws SQLException {
	    ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
	    ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bais);
            setObject(parameterIndex, ois.readObject());
        } catch (Exception e) {
            super.setBytes(parameterIndex, byteArray);
        }
    }

    @SuppressWarnings("unchecked")
	protected ResultSet executeQuery(Map params) throws SQLException {
		String sql = getSQL().toLowerCase().trim();
		AliasData aliasData = new AliasData();
		if (!sql.toLowerCase().startsWith("merge")) {
			aliasData = parse(sql);
		}

		MockResultSet rs = new MockResultSet("FOO");
		List<Param> requestParams = new ArrayList<Param>();
		for (Iterator<Map.Entry<Object, Object>> iter = params.entrySet()
				.iterator(); iter.hasNext();) {
			Param requestParam = new Param();
			Map.Entry<Object, Object> entry = iter.next();
			requestParam.setName(entry.getKey().toString());
			if (entry.getValue() != null) {
				requestParam.setValue(entry.getValue().toString());
			} else {
				requestParam.setValue("null");
			}
			requestParams.add(requestParam);
		}
		List<Param> catParams = new ArrayList<Param>();
		Param sqlSnippetParam = new Param();
		catParams.add(sqlSnippetParam);
		sqlSnippetParam.setName("sql");
		sqlSnippetParam.setValue(getSQL().toLowerCase());

		try {
			Param[] sqlResponse = _dataManager.findResponse(catParams,
					requestParams);
			if (sqlResponse == null) {
				return rs;
			}
			final Map<String, Integer> orderMap = aliasData.getOrderMap();
			Map<String, List<Object>> rsMap = new TreeMap<String, List<Object>>(
					new Comparator<String>() {

						public int compare(String o1, String o2) {
							if (orderMap.get(o1.toLowerCase()) == null
									|| orderMap.get(o2.toLowerCase()) == null) {
								if (!orderMap.containsKey("*")) {
									throw new RuntimeException(
											"Cannot have unknown columns without wildcards");
								}
							}
							if (orderMap.get(o1.toLowerCase()) == null
									&& orderMap.get(o2.toLowerCase()) == null) {
								return o1.toLowerCase().compareTo(
										o2.toLowerCase());
							}
							if (orderMap.get(o1.toLowerCase()) == null
									&& orderMap.get(o2.toLowerCase()) != null) {
								return orderMap.get("*").compareTo(
										orderMap.get(o2.toLowerCase()));
							}
							if (orderMap.get(o1.toLowerCase()) != null
									&& orderMap.get(o2.toLowerCase()) == null) {
								return orderMap.get(o1.toLowerCase())
										.compareTo(orderMap.get("*"));
							}
							return orderMap.get(o1.toLowerCase()).compareTo(
									orderMap.get(o2.toLowerCase()));
						}

					});
			int rowCount = 0;
			for (Param row : sqlResponse) {
				if (row.getName().equalsIgnoreCase("exception")) {
					throw new SQLException(row.getValue());
				}
				for (Param param : row.getParam()) {
					String paramTableName = param.getName().indexOf(".") > -1 ? param
							.getName()
							.substring(0, param.getName().indexOf("."))
							.toLowerCase()
							: null;
					String paramColumnName = param.getName()
							.substring(param.getName().indexOf(".") + 1)
							.toLowerCase();
					String columnName = paramColumnName;

					// check to see if param table name is alias
					if (paramTableName != null) {
						Map<String, String> tableAliases = aliasData
								.getTableAliases();
						if (tableAliases.containsKey(paramTableName)) {
							paramTableName = tableAliases.get(paramTableName);
						}

						// the configuration is specifying a table that
						// doesn't exist in the query
						if (!aliasData.getRealTableNames().contains(
								paramTableName)) {
							continue;
						}
					}
					Set<String> aliases = null;
					Map<String, Set<String>> columnAliases = aliasData
							.getColumnAliases();
					if (paramTableName != null) {
						aliases = columnAliases.get(paramTableName + "."
								+ paramColumnName);
					}
					if (aliases == null && aliasData.isUnambiguousColumnsOk()
							&& columnAliases.containsKey(paramColumnName)) {
						aliases = columnAliases.get(paramColumnName);
					}
					if (aliases == null) {
						aliases = new HashSet<String>();
					}
					if (aliases.isEmpty()) {
						aliases.add(columnName);
					}

					/*
					 * the order map will only contain the requested columns for
					 * example: select table.column from table order map will
					 * contain "column"
					 * 
					 * select table.column as foo from table order map will
					 * contain "foo"
					 */
					for (String alias : aliases) {
						if (!orderMap.containsKey(alias)) {
							if (orderMap.containsKey("*")) {
								if (paramTableName == null
										&& !aliasData.isUnambiguousColumnsOk()) {
									continue;
								}
								if (paramTableName != null
										&& !(aliasData.getTableAliases()
												.containsKey(paramTableName) || aliasData
												.getTableAliases()
												.containsValue(paramTableName))) {
									continue;
								}
							} else {
								continue;
							}
						}
						List<Object> values = rsMap.get(alias);
						if (values == null) {
							values = new ArrayList<Object>();
							rsMap.put(alias, values);
						} else if (values.size() > rowCount) {
							throw new SQLException(
									"Invalid configuration found in response to query "
											+ sql
											+ ". Too many rows for column "
											+ alias);
						}
						if ("null".equals(param.getValue())) {
							values.add(null);
						} else {
							values.add(convertToType(param.getValue(), param.getType()));
						}
					}
				}

				rowCount++;
			}
			for (Map.Entry<String, List<Object>> entry : rsMap.entrySet()) {
				rs.addColumn(entry.getKey(), entry.getValue());
			}
			setResultSets(new ResultSet[] { rs });
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		}
	}
	
	private Object convertToType(String value, String type) throws Exception {
	    if (type == null || value == null) {
	        return value;
	    }
        if ("SHORT".equals(type)) {
            return new Short(value);
        } else if ("INTEGER".equals(type)) {
            return new Integer(value);
        } else if ("LONG".equals(type)) {
            return new Long(value);
        } else if ("FLOAT".equals(type)) {
            return new Float(value);
        } else if ("DOUBLE".equals(type)) {
            return new Double(value);
        } else if ("BIG_DECIMAL".equals(type)) {
            return new BigDecimal(value);
        } else if ("BOOLEAN".equals(type)) {
            return new Boolean(value);
        } else if ("DATE".equals(type)) {
            return Date.valueOf(value);
        } else if ("TIME".equals(type)) {
            return Time.valueOf(value);
        } else if ("TIMESTAMP".equals(type)) {
            return Timestamp.valueOf(value);
        } else if ("URL".equals(type)) {
            return new URL(value);
        } else if ("BLOB".equals(type)) {
            return new SerialBlob(value.getBytes());
        } else if ("CLOB".equals(type)) {
            return new SerialClob(value.toCharArray());
        } else if ("ARRAY".equals(type)) {
            return new SerialArray(new MockArray(value));
        }
        return value;
	}

	public static AliasData parse(String sql) throws SQLException {
		AliasData data = new AliasData();
		if (sql == null) {
		    return data;
		}
		List<String> subSelects = new ArrayList<String>();
		if (sql.substring(1).contains("select ")) {
			// I'm going to assume that all sub-selects must be in parentheses
			int subSelectIndex = sql.substring(1).indexOf("select") + 1;
			while (subSelectIndex > -1) {
				if (sql.charAt(subSelectIndex) == '(') {
					break;
				}
				subSelectIndex--;
			}
			if (subSelectIndex != -1) {
				String remainderOfSql = sql.substring(subSelectIndex);
				StringBuilder sb = new StringBuilder(sql.substring(0,
						subSelectIndex));
				List<Character> openParenStack = new ArrayList<Character>();
				int currentOpenParenIdx = remainderOfSql.indexOf("(");
				openParenStack.add('(');
				remainderOfSql = remainderOfSql
						.substring(currentOpenParenIdx + 1);
				while (!openParenStack.isEmpty()) {
					currentOpenParenIdx = remainderOfSql.indexOf("(");
					int currentCloseParenIdx = remainderOfSql.indexOf(")");
					if (currentOpenParenIdx > -1
							&& currentCloseParenIdx > currentOpenParenIdx) {
						openParenStack.add('(');
						remainderOfSql = remainderOfSql
								.substring(currentOpenParenIdx + 1);
					} else {
						openParenStack.remove(openParenStack.size() - 1);
						remainderOfSql = remainderOfSql
								.substring(currentCloseParenIdx + 1);
					}
				}
				sb.append(" sub_select ");
				sb.append(remainderOfSql);
				sql = sb.toString();
				subSelects.add(sql);
			} else {
				subSelects.add(sql);
			}
		} else {
			subSelects.add(sql);
		}

        String selectPart = subSelects.get(0);
        String fromPart = null;
        int fromIdx = subSelects.get(0).indexOf(" from ");
        if (fromIdx > -1) {
            selectPart = subSelects.get(0).substring(0, fromIdx);
            fromPart = subSelects.get(0).substring(fromIdx);
        }
        if (fromPart != null) {
            if (fromPart.indexOf(" where ") > -1) {
                fromPart = fromPart.substring(0, fromPart.indexOf(" where "));
            }
        }
        if (fromPart.contains("join")) {
            String whereClause = fromPart.contains("where") ? fromPart
                    .substring(fromPart.indexOf("where")) : "";
            String matchString = whereClause.length() > 0 ? fromPart
                    .substring(0, fromPart.indexOf("where")) : fromPart;
            StringBuffer sb = new StringBuffer();
            Matcher m = _joinPattern.matcher(matchString);
            while (m.find()) {
                String tableName = m.group(1);
                m.appendReplacement(sb, ("," + tableName + " "));
                String remainder = m.appendTail(new StringBuffer())
                        .toString();
                if (remainder.contains(" on")
                        || remainder.contains(" union")) {
                    Matcher subM = _joinPattern.matcher(remainder);
                    String remainderStartWord = remainder.contains(" on") ? " on"
                            : " union";
                    String preMatch = remainder
                            .substring(
                                    0,
                                    remainder.indexOf(remainderStartWord) > 0 ? (remainder
                                            .indexOf(remainderStartWord) + 1)
                                            : 0);
                    if (subM.find()) {
                        remainder = preMatch
                                + remainder.substring(subM.start());
                    } else {
                        remainder = preMatch;
                    }
                    sb.append(remainder);
                    m = _joinPattern.matcher(sb.toString());
                    sb = new StringBuffer();
                }
            }
            m.appendTail(sb);
            sb.append(whereClause);
            fromPart = sb.toString();
        }
        fromPart = _fromReplacePattern.matcher(fromPart).replaceAll("");
        for (String fromItem : fromPart.split(",")) {
            String[] fromParts = _multiSpaceReplacePattern.matcher(fromItem.trim()).replaceAll(" ").split(" ");
            String table = fromParts[0];
            String alias = fromParts.length > 1 ? fromParts[fromParts.length - 1] : null;
            if (alias != null) {
                data.getTableAliases().put(alias, table);
            }
            data.getRealTableNames().add(table);
        }

        List<String> functionList = removeFunctions(selectPart);
        selectPart = functionList.get(0);
        int caseStatementCount = 0;
        while (selectPart.contains("case when")) {
            StringBuilder sb = new StringBuilder(selectPart.substring(0,
                    selectPart.indexOf("case when")));
            sb.append("case.case_statement_");
            sb.append(++caseStatementCount);
            sb.append(selectPart.substring(selectPart.indexOf("end", sb
                    .toString().length())
                    + "end".length()));
            selectPart = sb.toString();
        }
        selectPart = _selectReplacePattern.matcher(selectPart).replaceAll("");
        data.setUnambiguousColumnsOk(data.isUnambiguousColumnsOk()
                && buildColumnAliasInfo(selectPart, data));
        return data;
	}

    @SuppressWarnings("unchecked")
	protected int executeUpdate(Map params) throws SQLException {
		Test test = new Test();
		test.setRequest(new Request());
		test.setResponse(new Response());
		Param responseParam = new Param();
		responseParam.setName("captured data response");
		responseParam.setValue("");
		test.getResponse().addParam(responseParam);
		List<Param> requestParams = new ArrayList<Param>();
		String tableName = null;
		String sql = getSQL();
		if (getSQL().toLowerCase().startsWith("create")
				|| getSQL().toLowerCase().startsWith("drop")
				|| getSQL().toLowerCase().startsWith("shutdown")) {
			return 0;
		} else if (getSQL().toLowerCase().startsWith("merge")) {
			tableName = sql.split(" ")[2];
			for (Object key : params.keySet()) {
				Object value = params.get(key);
				Param requestParam = new Param();
				requestParam.setName(key.toString());
				if (value == null)
					requestParam.setValue(null);
				else
					requestParam.setValue(value.toString());
				requestParams.add(requestParam);
			}
			_dataManager
					.logRequestParams(new ArrayList<Param>(), requestParams);
			test.getRequest().setParam(
					requestParams.toArray(new Param[requestParams.size()]));
			_dataManager.addCapturedData(tableName, test);
			return 1;
		}

		if (sql.toLowerCase().startsWith("insert ")) {
		    String table = null;
		    String columns = null;
		    String values = null;
		    Matcher m = _insertPattern1.matcher(sql);
		    if (m.matches()) {
		        table = m.group(1);
		        columns = m.group(2);
		        List<String> functions = removeFunctions(m.group(3));
		        values = functions.get(0);
		    } else {
	            m = _insertPattern2.matcher(sql);
	            if (m.matches()) {
	                table = m.group(1);
	                List<String> functions = removeFunctions(m.group(2));
	                values = functions.get(0);
	            }
		    }
		    if (table == null) {
	            Logger.getLogger(getClass()).error("Unable to parse SQL " + sql);
	            throw new SQLException("Unable to parse SQL", sql);
		    }
		    String[] valueCollection = _singleSpaceReplacePattern.matcher(values).replaceAll("").split(",");
		    String[] columnCollection = columns != null ? _singleSpaceReplacePattern.matcher(columns).replaceAll("").split(",") : null;
		    if (columnCollection == null) {
		        columnCollection = new String[valueCollection.length];
		        for (int i = 0; i < valueCollection.length; i++) {
		            columnCollection[i] = Integer.toString(i + 1);
		        }
		    }
            int paramIdx = 1, colIdx = 0;
            for (String stmtValue : valueCollection) {
                Param requestParam = new Param();
                Object value = null;
                if (!stmtValue.contains("?")) {
                    value = _quoteReplacePattern.matcher(_tickReplacePattern.matcher(stmtValue).replaceAll("")).replaceAll("");
                } else {
                    value = params.get(paramIdx++);
                }
                String columnName = (String) columnCollection[colIdx++];
                requestParam.setName(columnName);
                if (value != null) {
                    requestParam.setValue(value.toString());
                }
                requestParams.add(requestParam);
            }
			// look for generated sequence
			List<Param> catParams = new ArrayList<Param>();
			Param sqlSnippetParam = new Param();
			catParams.add(sqlSnippetParam);
			sqlSnippetParam.setName("sql");
			sqlSnippetParam.setValue(sql.toLowerCase());
			try {
				Param[] sqlResponse = _dataManager.findResponse(catParams,
						requestParams);
				MockResultSet rs = null;
				if (sqlResponse != null && sqlResponse.length == 1) {
					if (sqlResponse[0].getName().equalsIgnoreCase("exception")) {
						throw new SQLException(sqlResponse[0].getValue());
					}
					rs = new MockResultSet("FOO");
					rs.addColumn("generated_key", new Object[] { sqlResponse[0]
							.getParam(0).getValue() });
				}
				setLastGeneratedKeysResultSet(rs);
			} catch (Exception e) {
				Logger.getLogger(getClass()).error(
						"Error creating generated key result set", e);
				throw new SQLException(e.getMessage());
			}
		} else if (sql.toLowerCase().startsWith("update ")) {
		    Matcher m = _updatePattern.matcher(sql);
		    String table = null;
		    String remainder = null;
		    if (m.matches()) {
		        table = m.group(1);
		        remainder = m.group(2);
		    }
            if (table == null) {
                Logger.getLogger(getClass()).error("Unable to parse SQL " + sql);
                throw new SQLException("Unable to parse SQL", sql);
            }
            if (remainder.toLowerCase().indexOf(" where ") > -1) {
                remainder = remainder.substring(0, remainder.toLowerCase().indexOf(" where"));
            }
            List<String> functionList = removeFunctions(_singleSpaceReplacePattern.matcher(remainder).replaceAll(""));
            remainder = functionList.get(0);
            Map updateStatementMap = new ListOrderedMap();
            for (String updatePart : remainder.split(",")) {
                String[] parts = updatePart.split("=");
                if (parts.length != 2) {
                    Logger.getLogger(getClass()).error("Unable to parse SQL " + sql);
                    throw new SQLException("Unable to parse SQL", sql);
                }
                updateStatementMap.put(parts[0], _quoteReplacePattern.matcher(_tickReplacePattern.matcher(parts[1]).replaceAll("")).replaceAll(""));
            }
            int paramIdx = 1;
            for (Object foo : updateStatementMap.entrySet()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)foo;
                Param requestParam = new Param();
                requestParam.setName(entry.getKey());
                if (!entry.getValue().contains("?")) {
                    String tmpParamValue = entry.getValue();
                    String paramValue = entry.getValue();
                    Matcher functionMatcher = _functionPattern.matcher(tmpParamValue);
                    while (functionMatcher.find()) {
                        String functionSection = functionList.get(Integer.parseInt(functionMatcher.group(1)));
                        int fnParamIdx = functionSection.indexOf('?');
                        while (fnParamIdx > -1) {
                            Object paramVal = params.get(paramIdx++);
                            functionSection = _sqlParamReplacePattern.matcher(functionSection).replaceFirst(paramVal == null ? "null" : paramVal.toString());
                            fnParamIdx = functionSection.indexOf('?');
                        }
                        paramValue = _functionPattern.matcher(paramValue).replaceFirst(functionSection);
                    }
                    requestParam.setValue(paramValue);
                } else {
                    Object paramVal = params.get(paramIdx++);
                    String paramString = paramVal == null ? "null" : paramVal.toString();
                    requestParam.setValue(paramString);
                }
                requestParams.add(requestParam);
            }

			Param sqlSnippetParam = new Param();
			requestParams.add(sqlSnippetParam);
			sqlSnippetParam.setName("sql snippet");
			sqlSnippetParam.setValue(getSQL());
			_dataManager
					.logRequestParams(new ArrayList<Param>(), requestParams);
		}
		test.getRequest().setParam(
				requestParams.toArray(new Param[requestParams.size()]));
		_dataManager.addCapturedData(tableName, test);
		return 1;
	}

	void setCustomFunctionMap(Map<String, Integer> customFunctionMap) {
	}
	
	// returns a List that contains, as its first entry, the
	// sql with the function removed and, as the remaining entries,
	// the functions arguments
	private static List<String> removeFunctions(String part) {
        int openParenIdx = part.indexOf("(");
        List<String> functions = new ArrayList<String>();
        while (openParenIdx > -1) {
            String remainderOfSql = part.substring(openParenIdx);
            StringBuilder sb = new StringBuilder(part.substring(0,
                    openParenIdx));
            List<Character> openParenStack = new ArrayList<Character>();
            int currentOpenParenIdx = remainderOfSql.indexOf("(");
            openParenStack.add('(');
            StringBuilder functionSb = new StringBuilder("(");
            remainderOfSql = remainderOfSql
                    .substring(currentOpenParenIdx + 1);
            while (!openParenStack.isEmpty()) {
                currentOpenParenIdx = remainderOfSql.indexOf("(");
                int currentCloseParenIdx = remainderOfSql.indexOf(")");
                if (currentOpenParenIdx > -1
                        && currentCloseParenIdx > currentOpenParenIdx) {
                    openParenStack.add('(');
                    functionSb.append(remainderOfSql.substring(0, currentOpenParenIdx +1));
                    remainderOfSql = remainderOfSql
                            .substring(currentOpenParenIdx + 1);
                } else {
                    openParenStack.remove(openParenStack.size() - 1);
                    functionSb.append(remainderOfSql.substring(0, currentCloseParenIdx +1));
                    remainderOfSql = remainderOfSql
                            .substring(currentCloseParenIdx + 1);
                }
            }
            functions.add(functionSb.toString());
            sb.append("[FN").append(functions.size()).append("]").append(remainderOfSql);
            part = sb.toString();
            openParenIdx = part.indexOf("(");
        }
        functions.add(0, part);
        return functions;
    }

    private static boolean buildColumnAliasInfo(String selectPart, AliasData aliasData) {
	    boolean unambiguousColumnsOk = true;
	    int colCount = 1;
	    Set<String> realColumnNames = new HashSet<String>();
	    for (String selectItem : selectPart.split(",")) {
	        String[] selectItemParts = selectItem.trim().split(" ");
	        String columnTableName = null;
	        String columnName = selectItemParts[0];
	        if (columnName.contains(".")) {
	            columnTableName = columnName.substring(0, columnName.indexOf('.'));
	            columnName = columnName.substring(columnName.indexOf('.') + 1);
	        }
	        String alias = selectItemParts.length > 1 ? selectItemParts[selectItemParts.length - 1] : null;
            String realFullName = null;
            if (columnTableName != null) {
                Map<String, String> tableAliases = aliasData.getTableAliases();
                String realTableName = tableAliases.containsKey(columnTableName) ? tableAliases
                        .get(columnTableName) : columnTableName;
                realFullName = new StringBuilder(realTableName).append(".")
                        .append(columnName).toString();
            }
            String orderName = alias == null ? columnName : alias;
            for (String key : realColumnNames) {
                String[] tmp = key.split("\\.");
                if (tmp[1].equals(columnName)
                        && !tmp[0].equals(String.valueOf(columnTableName))) {
                    unambiguousColumnsOk = false;
                }
            }
            realColumnNames.add(columnTableName + "." + columnName);
            Map<String, Set<String>> columnAliases = aliasData
            .getColumnAliases();
            Set<String> aliasList = columnAliases.get(columnName);
            if (aliasList == null) {
                aliasList = new HashSet<String>();
                columnAliases.put(columnName, aliasList);
            }
            if (alias != null) {
                aliasList.add(alias);
            }
        
            // add aliases with "real table name"."real column name"
            if (realFullName != null) {
                aliasList = columnAliases.get(realFullName);
                if (aliasList == null) {
                    aliasList = new HashSet<String>();
                    columnAliases.put(realFullName, aliasList);
                }
                if (alias != null) {
                    aliasList.add(alias);
                }
            }
        
            aliasData.getOrderMap().put(orderName, colCount);
            colCount++;
	    }
	    return unambiguousColumnsOk;
	}

	/*
	private static boolean buildColumnAliasInfo(ZQuery queryStatement,
			AliasData aliasData) {
		boolean unambiguousColumnsOk = true;
		int colCount = 1;
		Set<String> realColumnNames = new HashSet<String>();
		for (Object o : queryStatement.getSelect()) {
			ZSelectItem selectItem = (ZSelectItem) o;
			String columnName = selectItem.getColumn().toLowerCase();
			String alias = selectItem.getAlias() == null ? null : selectItem
					.getAlias().toLowerCase();
			String columnTableName = selectItem.getTable();
			String realFullName = null;
			if (columnTableName != null) {
				Map<String, String> tableAliases = aliasData.getTableAliases();
				String realTableName = tableAliases.containsKey(selectItem
						.getTable().toLowerCase()) ? tableAliases
						.get(selectItem.getTable().toLowerCase()) : selectItem
						.getTable().toLowerCase();
				realFullName = new StringBuilder(realTableName).append(".")
						.append(columnName).toString();
			}
			String orderName = alias == null ? columnName : alias;

			for (String key : realColumnNames) {
				String[] tmp = key.split("\\.");
				if (tmp[1].equals(columnName)
						&& !tmp[0].equals(String.valueOf(columnTableName))) {
					unambiguousColumnsOk = false;
				}
			}
			realColumnNames.add(columnTableName + "." + columnName);
			// add aliases with "real column name"
			Map<String, Set<String>> columnAliases = aliasData
					.getColumnAliases();
			Set<String> aliasList = columnAliases.get(columnName);
			if (aliasList == null) {
				aliasList = new HashSet<String>();
				columnAliases.put(columnName, aliasList);
			}
			if (selectItem.getAlias() != null) {
				aliasList.add(selectItem.getAlias().toLowerCase());
			}

			// add aliases with "real table name"."real column name"
			if (realFullName != null) {
				aliasList = columnAliases.get(realFullName);
				if (aliasList == null) {
					aliasList = new HashSet<String>();
					columnAliases.put(realFullName, aliasList);
				}
				if (selectItem.getAlias() != null) {
					aliasList.add(alias);
				}
			}

			aliasData.getOrderMap().put(orderName, colCount);
			colCount++;
		}
		return unambiguousColumnsOk;
	}
	*/

}
