/* *****************************************************************************
 * Copyright (C) 2021, Prasanth R. Pasala, Brian E. Pangburn, & The Pangburn Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Contributors:
 *   Prasanth R. Pasala
 *   Brian E. Pangburn
 *   Diego Gil
 *   Man "Bee" Vo
 *   Ernie R. Rael
 * ****************************************************************************/
package com.nqadmin.swingset.datasources;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.nqadmin.swingset.datasources.JdbcDataTypeConversionTables.*;

/**
 * verify the tables
 */
public class JdbcDataTypeConversionTablesTest {
	
	/**
	 * constructor
	 */
	public JdbcDataTypeConversionTablesTest() {
	}
	
	/**
	 * doc
	 */
	@BeforeAll
	public static void setUpClass() {
	}
	
	/**
	 * doc
	 */
	@AfterAll
	public static void tearDownClass() {
	}
	
	/**
	 * doc
	 */
	@BeforeEach
	public void setUp() {
	}
	
	/**
	 * doc
	 */
	@AfterEach
	public void tearDown() {
	}


	String getListAsString(List<String> l) {
		StringBuilder sb = new StringBuilder();
		l.forEach((s) -> sb.append(s).append('\n'));
		return sb.toString();
	}

	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	void createExpectedResult(List<String> l) {
		l.forEach((s) -> System.err.println("+ \"" + s + "\\n\""));
	}
	
	/**
	 * Test of jdbcTypeToClass method, of class JdbcDataTypeConversionTables.
	 */
	@Test
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public void testJdbcTypeToClass() {
		System.out.println("jdbcTypeToClass");
		List<String> l = new ArrayList<>();
		for (JDBCType type : JDBCType.values()) {
			Class<?> clazz = jdbcTypeToClass(type);
			if(clazz == null)
				continue;
			String s = getStringVal(clazz);
			l.add(String.format("%s %s", type, s));
		}
		//createExpectedResult(l);
		String expResult = jdbcTypeToClassExpected;
		String result = getListAsString(l);
		assertEquals(expResult, result);
	}

	/**
	 * Test of jdbcTypeToClassStrict method, of class JdbcDataTypeConversionTables.
	 */
	@Test
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public void testJdbcTypeToClassStrict() {
		System.out.println("jdbcTypeToClassStrict");
		List<String> l = new ArrayList<>();
		for (JDBCType type : JDBCType.values()) {
			Class<?> clazz = jdbcTypeToClassStrict(type);
			if(clazz == null)
				continue;
			String s = getStringVal(clazz);
			l.add(String.format("%s %s", type, s));
		}
		//createExpectedResult(l);
		String expResult = jdbcTypeToClassStrictExpected;
		String result = getListAsString(l);
		assertEquals(expResult, result);
	}

	/**
	 * Test of classToJdbcType method, of class JdbcDataTypeConversionTables.
	 */
	@Test
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public void testClassToJdbcType() {
		System.out.println("classToJdbcType");
		List<String> l = new ArrayList<>();
		for (Class<?> clazz : classToJdbcType.asMap().keySet()) {
			JDBCType type = classToJdbcType(clazz);
			if(type == null)
				continue;
			String s = getStringVal(clazz);
			l.add(String.format("%s %s", s, type.getName()));
		}
		JDBCType type = classToJdbcType(this.getClass());
		assertNull(type);
		//createExpectedResult(l);
		String expResult = classToJdbcTypeExpected;
		String result = getListAsString(l);
		assertEquals(expResult, result);
	}

	/**
	 * Test of specTableToString method, of class JdbcDataTypeConversionTables.
	 */
	@Test
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public void testTable3() {
		System.out.println("Table3");
		Map<?,?> m = jdbcTypeToClass;
		String expResult = table3;
		String result = getListAsString(specTableToOutputList(m));
		assertEquals(expResult, result);
		//System.err.println(result);
	}

	/**
	 * Test of specTableToOutputList method, of class JdbcDataTypeConversionTables.
	 */
	@Test
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public void testTable4() {
		System.out.println("Table4");
		Map<?,?> m = classToJdbcType.asMap();
		String expResult = table4;
		String result = getListAsString(specTableToOutputList(m));
		assertEquals(expResult, result);
		//System.err.println(result);
	}

	//public static String specTableToString(Map<?,?> m) {
	//	List<String> l = specTableToOutputList(m);
	//	StringBuilder sb = new StringBuilder();
	//	l.forEach((s) -> sb.append(s).append('\n'));
	//	return sb.toString();
	//}

	/**
	 * create list of strings from map
	 * @param m map
	 * @return list of strings
	 */
	public static List<String> specTableToOutputList(Map<?,?> m) {
		int outputWidth = 80;
		// first get the max width of ascii key.
		OptionalInt max = m.keySet().stream().mapToInt((k) -> getStringVal(k).length()).max();
		if (!max.isPresent())
			return Collections.emptyList();
		StringBuilder sb = new StringBuilder();
		int keyWidth = max.getAsInt(); // max plus two spaces
		List<String> output = new ArrayList<>();
		
		m.forEach((key,_list) -> {
			sb.append(String.format("%-"+keyWidth+"s ", getStringVal(key)));
			Collection<?> list = (Collection<?>) _list;
			for (Object o : list) {
				String next = getStringVal(o);
				if (sb.length() + 1 + next.length() >= outputWidth) {
					output.add(sb.toString());
					sb.setLength(0);
					sb.append(String.format("%"+keyWidth+"s ", ""));
				}
				sb.append(' ').append(next);
			}
			output.add(sb.toString());
			sb.setLength(0);
		});

		return output;
	}

	private static String getStringVal(Object o) {
		if (o == null)
			return "";

		String s;
		if (o instanceof Class) {
			String t = ((Class<?>)o).getName();
			s = (t.contains("sql")) ? t : ((Class<?>)o).getSimpleName();
		} else if (o instanceof JDBCType) {
			s = ((JDBCType)o).getName();
		} else {
			s = o.toString();
		}
		return s;
	}

	String table3
			= "BIT                      Boolean\n"
			+ "TINYINT                  Byte Integer\n"
			+ "SMALLINT                 Short Integer\n"
			+ "INTEGER                  Integer\n"
			+ "BIGINT                   Long\n"
			+ "FLOAT                    Double\n"
			+ "REAL                     Float\n"
			+ "DOUBLE                   Double\n"
			+ "NUMERIC                  BigDecimal\n"
			+ "DECIMAL                  BigDecimal\n"
			+ "CHAR                     String\n"
			+ "VARCHAR                  String\n"
			+ "LONGVARCHAR              String\n"
			+ "DATE                     LocalDate java.sql.Date\n"
			+ "TIME                     LocalTime java.sql.Time\n"
			+ "TIMESTAMP                LocalDateTime java.sql.Timestamp\n"
			+ "BINARY                   byte[]\n"
			+ "VARBINARY                byte[]\n"
			+ "LONGVARBINARY            byte[]\n"
			+ "ARRAY                    java.sql.Array\n"
			+ "BLOB                     java.sql.Blob\n"
			+ "CLOB                     java.sql.Clob\n"
			+ "REF                      java.sql.Ref\n"
			+ "DATALINK                 URL\n"
			+ "BOOLEAN                  Boolean\n"
			+ "ROWID                    java.sql.RowId\n"
			+ "NCHAR                    String\n"
			+ "NVARCHAR                 String\n"
			+ "LONGNVARCHAR             String\n"
			+ "NCLOB                    java.sql.NClob\n"
			+ "SQLXML                   java.sql.SQLXML\n"
			+ "TIME_WITH_TIMEZONE       OffsetTime\n"
			+ "TIMESTAMP_WITH_TIMEZONE  OffsetDateTime\n"
			;
	
	String table4
			= "String              CHAR VARCHAR LONGVARCHAR NCHAR NVARCHAR LONGNVARCHAR\n"
			+ "BigDecimal          NUMERIC\n"
			+ "Boolean             BIT BOOLEAN\n"
			+ "Byte                TINYINT\n"
			+ "Short               SMALLINT\n"
			+ "Integer             INTEGER\n"
			+ "Long                BIGINT\n"
			+ "Float               REAL\n"
			+ "Double              DOUBLE FLOAT\n"
			+ "byte[]              BINARY VARBINARY LONGVARBINARY\n"
			+ "BigInteger          BIGINT\n"
			+ "java.sql.Date       DATE\n"
			+ "java.sql.Time       TIME\n"
			+ "java.sql.Timestamp  TIMESTAMP\n"
			+ "java.sql.Clob       CLOB\n"
			+ "java.sql.Blob       BLOB\n"
			+ "java.sql.Array      ARRAY\n"
			+ "java.sql.Struct     STRUCT\n"
			+ "java.sql.Ref        REF\n"
			+ "URL                 DATALINK\n"
			+ "java.sql.RowId      ROWID\n"
			+ "java.sql.NClob      NCLOB\n"
			+ "java.sql.SQLXML     SQLXML\n"
			+ "Calendar            TIMESTAMP\n"
			+ "Date                TIMESTAMP\n"
			+ "LocalDate           DATE\n"
			+ "LocalTime           TIME\n"
			+ "LocalDateTime       TIMESTAMP\n"
			+ "OffsetTime          TIME_WITH_TIMEZONE\n"
			+ "OffsetDateTime      TIMESTAMP_WITH_TIMEZONE\n"
			;

	String jdbcTypeToClassExpected =
			  "BIT Boolean\n"
			+ "TINYINT Byte\n"
			+ "SMALLINT Short\n"
			+ "INTEGER Integer\n"
			+ "BIGINT Long\n"
			+ "FLOAT Double\n"
			+ "REAL Float\n"
			+ "DOUBLE Double\n"
			+ "NUMERIC BigDecimal\n"
			+ "DECIMAL BigDecimal\n"
			+ "CHAR String\n"
			+ "VARCHAR String\n"
			+ "LONGVARCHAR String\n"
			+ "DATE LocalDate\n"
			+ "TIME LocalTime\n"
			+ "TIMESTAMP LocalDateTime\n"
			+ "BINARY byte[]\n"
			+ "VARBINARY byte[]\n"
			+ "LONGVARBINARY byte[]\n"
			+ "ARRAY java.sql.Array\n"
			+ "BLOB java.sql.Blob\n"
			+ "CLOB java.sql.Clob\n"
			+ "REF java.sql.Ref\n"
			+ "DATALINK URL\n"
			+ "BOOLEAN Boolean\n"
			+ "ROWID java.sql.RowId\n"
			+ "NCHAR String\n"
			+ "NVARCHAR String\n"
			+ "LONGNVARCHAR String\n"
			+ "NCLOB java.sql.NClob\n"
			+ "SQLXML java.sql.SQLXML\n"
			+ "TIME_WITH_TIMEZONE OffsetTime\n"
			+ "TIMESTAMP_WITH_TIMEZONE OffsetDateTime\n"
			;

	String jdbcTypeToClassStrictExpected =
			  "BIT Boolean\n"
			+ "TINYINT Integer\n"
			+ "SMALLINT Integer\n"
			+ "INTEGER Integer\n"
			+ "BIGINT Long\n"
			+ "FLOAT Double\n"
			+ "REAL Float\n"
			+ "DOUBLE Double\n"
			+ "NUMERIC BigDecimal\n"
			+ "DECIMAL BigDecimal\n"
			+ "CHAR String\n"
			+ "VARCHAR String\n"
			+ "LONGVARCHAR String\n"
			+ "DATE java.sql.Date\n"
			+ "TIME java.sql.Time\n"
			+ "TIMESTAMP java.sql.Timestamp\n"
			+ "BINARY byte[]\n"
			+ "VARBINARY byte[]\n"
			+ "LONGVARBINARY byte[]\n"
			+ "ARRAY java.sql.Array\n"
			+ "BLOB java.sql.Blob\n"
			+ "CLOB java.sql.Clob\n"
			+ "REF java.sql.Ref\n"
			+ "DATALINK URL\n"
			+ "BOOLEAN Boolean\n"
			+ "ROWID java.sql.RowId\n"
			+ "NCHAR String\n"
			+ "NVARCHAR String\n"
			+ "LONGNVARCHAR String\n"
			+ "NCLOB java.sql.NClob\n"
			+ "SQLXML java.sql.SQLXML\n"
			;

	String classToJdbcTypeExpected =
			  "String CHAR\n"
			+ "BigDecimal NUMERIC\n"
			+ "Boolean BIT\n"
			+ "Byte TINYINT\n"
			+ "Short SMALLINT\n"
			+ "Integer INTEGER\n"
			+ "Long BIGINT\n"
			+ "Float REAL\n"
			+ "Double DOUBLE\n"
			+ "byte[] BINARY\n"
			+ "BigInteger BIGINT\n"
			+ "java.sql.Date DATE\n"
			+ "java.sql.Time TIME\n"
			+ "java.sql.Timestamp TIMESTAMP\n"
			+ "java.sql.Clob CLOB\n"
			+ "java.sql.Blob BLOB\n"
			+ "java.sql.Array ARRAY\n"
			+ "java.sql.Struct STRUCT\n"
			+ "java.sql.Ref REF\n"
			+ "URL DATALINK\n"
			+ "java.sql.RowId ROWID\n"
			+ "java.sql.NClob NCLOB\n"
			+ "java.sql.SQLXML SQLXML\n"
			+ "Calendar TIMESTAMP\n"
			+ "Date TIMESTAMP\n"
			+ "LocalDate DATE\n"
			+ "LocalTime TIME\n"
			+ "LocalDateTime TIMESTAMP\n"
			+ "OffsetTime TIME_WITH_TIMEZONE\n"
			+ "OffsetDateTime TIMESTAMP_WITH_TIMEZONE\n"
			;
}
