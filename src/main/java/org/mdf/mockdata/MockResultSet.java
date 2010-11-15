package org.mdf.mockdata;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;


public class MockResultSet extends com.mockrunner.mock.jdbc.MockResultSet {

    public MockResultSet(String id) {
        super(id);
    }

    public MockResultSet(String id, String cursorName) {
        super(id, cursorName);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar calendar) throws SQLException {
        Timestamp ts = super.getTimestamp(columnIndex);
        if (ts == null)
            return null;
        Calendar compareCal = Calendar.getInstance();
        compareCal.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
        Period p = Period.fieldDifference(LocalDateTime.fromCalendarFields(calendar), LocalDateTime.fromCalendarFields(compareCal));
        DateTime dt = new DateTime(ts.getTime()).plus(p);
        return new Timestamp(dt.getMillis());
    }

    @Override
    public Timestamp getTimestamp(String columnName, Calendar calendar) throws SQLException {
        Timestamp ts = super.getTimestamp(columnName);
        if (ts == null)
            return null;
        Calendar compareCal = Calendar.getInstance();
        compareCal.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
        Period p = Period.fieldDifference(LocalDateTime.fromCalendarFields(calendar), LocalDateTime.fromCalendarFields(compareCal));
        DateTime dt = new DateTime(ts.getTime()).plus(p);
        return new Timestamp(dt.getMillis());
    }

    @Override
    // I'm not sure I've mentioned recently how much I hate hibernate...
    public byte[] getBytes(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if(value instanceof byte[]) return (byte[])value;
        try {
            return serialize(value);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(getClass()).warn("Unable to create bytes for value " + value, e);
            throw new SQLException("Unable to create bytes for value " + value);
        }
    }

    @Override
    // I'm not sure I've mentioned recently how much I hate hibernate...
    public byte[] getBytes(int columnIndex) throws SQLException {
        Object value = getObject(columnIndex);
        if(value instanceof byte[]) return (byte[])value;
        try {
            return serialize(value);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(getClass()).warn("Unable to create bytes for value " + value, e);
            throw new SQLException("Unable to create bytes for value " + value);
        }
    }
    
    private byte[] serialize(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return baos.toByteArray();
    }

}
