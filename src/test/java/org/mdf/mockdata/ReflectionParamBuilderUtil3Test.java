package org.mdf.mockdata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.mdf.mockdata.generated.Param;
import org.testng.annotations.Test;

public class ReflectionParamBuilderUtil3Test {

    @Test
    public void testUTCDateTimeZone() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(DateTimeZone.UTC, objectParam);
        printParam(objectParam);
        DateTimeZone rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTimeZone.class);
        assertEquals(DateTimeZone.UTC, rehydrated);
    }

    @Test
    public void testDefaultDateTimeZone() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(DateTimeZone.getDefault(), objectParam);
        DateTimeZone rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTimeZone.class);
        assertEquals(DateTimeZone.getDefault(), rehydrated);
    }

    @Test
    public void testNewYorkTimeZone() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        DateTimeZone newYork = DateTimeZone.forID("America/New_York");
        ReflectionParamBuilderUtil3.buildParamFromObject(newYork, objectParam);
        printParam(objectParam);
        DateTimeZone rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTimeZone.class);
        assertEquals(newYork, rehydrated);
    }

    @Test
    public void testDateTimeWithoutReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        DateTime dt = new DateTime();
        ReflectionParamBuilderUtil3.buildParamFromObject(dt, objectParam, false);
        printParam(objectParam);
        DateTime rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTime.class);
        assertEquals(dt, rehydrated);
    }

    @Test
    public void testDateTimeNow() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        DateTime dt = new DateTime();
        ReflectionParamBuilderUtil3.buildParamFromObject(dt, objectParam);
        printParam(objectParam);
        DateTime rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTime.class);
        assertEquals(dt, rehydrated);
        rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTime.class);
        assertEquals(dt, rehydrated);
    }

    @Test
    public void testDateTimePast() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        DateTime dt = new DateTime(System.currentTimeMillis() - 6000000);
        ReflectionParamBuilderUtil3.buildParamFromObject(dt, objectParam);
        DateTime rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, DateTime.class);
        assertEquals(dt, rehydrated);
    }

    @Test
    public void testMultipleSameDateTimes() throws Exception {
        long currentTimeMillis = DateTimeUtils.currentTimeMillis();

        // with references these would never be able to compare because some of
        // the
        // sub objects end up with different hashcodes (ISOChronology$Stub for
        // sure)
        DateTime dt = new DateTime(currentTimeMillis);
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(dt, objectParam, false);

        DateTime dt2 = new DateTime(currentTimeMillis);
        Param objectParam2 = new Param();
        objectParam2.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(dt2, objectParam2, false);

        DefaultParameterMatcher defaultParameterMatcher = new DefaultParameterMatcher();
        List<Param> reqParams = new ArrayList<Param>();
        reqParams.add(objectParam);
        assertTrue(
                "Two joda date times created with same args should compare equal according to DefaultParameterMatcher",
                defaultParameterMatcher.paramsMatch(reqParams, new Param[] { objectParam2 }));
    }

    @Test
    public void testGregorianCalendar() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        Calendar gc = GregorianCalendar.getInstance();
        ReflectionParamBuilderUtil3.buildParamFromObject(gc, objectParam);
        printParam(objectParam);
        GregorianCalendar rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam,
                GregorianCalendar.class);
        assertEquals(gc, rehydrated);
    }

    @Test
    public void testRandom() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        Random random = new Random(42);
        ReflectionParamBuilderUtil3.buildParamFromObject(random, objectParam);
        printParam(objectParam);
        Random rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Random.class);
        Field seedField = Random.class.getDeclaredField("seed");
        seedField.setAccessible(true);
        assertEquals(((AtomicLong) seedField.get(random)).longValue(), ((AtomicLong) seedField.get(rehydrated))
                .longValue());
    }

    @Test
    public void testBuildParamFromObject() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(new ComplexClass(new HashMap<String, ComplexClass2>(), true),
                objectParam);
        printParam(objectParam);
    }

    @Test
    public void testBuildParamFromPrimitive() throws Exception {
        // string
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject("This is a simple string", objectParam, false);
        printParam(objectParam);
        String rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, String.class);
        assertEquals("This is a simple string", rehydrated);

        // int
        objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(1, objectParam, false);
        printParam(objectParam);
        int rehydratedInt = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, int.class);
        assertEquals(1, rehydratedInt);

        // float
        objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(1.24f, objectParam, false);
        printParam(objectParam);
        float rehydratedFloat = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, float.class);
        assertEquals(1.24f, rehydratedFloat);

        // boolean
        objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(true, objectParam, false);
        printParam(objectParam);
        boolean rehydratedBool = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, boolean.class);
        assertTrue(rehydratedBool);

        // null
        objectParam = new Param();
        objectParam.setName("objectParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(null, objectParam, false);
        printParam(objectParam);
        Object o = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Object.class);
        assertNull(o);
    }

    @Test
    public void testBuildParamFromSimpleObject() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        MyConcreteOne obj = new MyConcreteOne();
        obj.setString("Test String");
        ReflectionParamBuilderUtil3.buildParamFromObject(obj, objectParam, false);
        printParam(objectParam);
        MyConcreteOne rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, MyConcreteOne.class);
        assertEquals(obj, rehydrated);
    }

    @Test
    public void testBuildParamFromObjectWithoutReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ComplexClass original = new ComplexClass(new HashMap<String, ComplexClass2>(), true);
        ReflectionParamBuilderUtil3.buildParamFromObject(original, objectParam, false);
        printParam(objectParam);
        ComplexClass rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, ComplexClass.class);
        assertEquals(original, rehydrated);
        assertEquals(original.hashCode(), rehydrated.hashCode());
    }

    @Test
    public void testBuildObjectFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        ComplexClass original = new ComplexClass(new HashMap<String, ComplexClass2>(), true);
        ReflectionParamBuilderUtil3.buildParamFromObject(original, objectParam);
        printParam(objectParam);
        ComplexClass rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, ComplexClass.class);
        assertEquals(original, rehydrated);
        assertEquals(original.hashCode(), rehydrated.hashCode());
    }

    @Test
    public void testComplexParamWithValueSetViaReflection() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        HashMap<String, ComplexClass2> complexClassMap = new HashMap<String, ComplexClass2>();
        complexClassMap.put("complex class in map", new ComplexClass2(null));
        ComplexClass original = new ComplexClass(complexClassMap, true);
        Field f = original.getClass().getDeclaredField("_c2");
        f.setAccessible(true);
        f.set(original, new ComplexClass2("set via reflection"));
        ReflectionParamBuilderUtil3.buildParamFromObject(original, objectParam);
        printParam(objectParam);
        ComplexClass rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, ComplexClass.class);
        assertEquals(original, rehydrated);
        assertEquals(original.hashCode(), rehydrated.hashCode());
    }

    @Test
    public void testReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        MyConcreteOne s = new MyConcreteOne("Hi");
        Collection<MyConcreteOne> myList = new ArrayList<MyConcreteOne>();
        myList.add(s);
        myList.add(s);
        ReflectionParamBuilderUtil3.buildParamFromObject(myList, objectParam);
        printParam(objectParam);
        ArrayList<MyConcreteOne> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam,
                ArrayList.class);
        assertTrue(rehydrated.get(0) == rehydrated.get(1));
    }

    @Test
    public void testBuildPrimitiveFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("primitiveParam");
        boolean original = true;
        ReflectionParamBuilderUtil3.buildParamFromObject(original, objectParam);
        printParam(objectParam);
        boolean rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, boolean.class);
        assertEquals(original, rehydrated);
    }

    @Test
    public void testBuildEmptyArrayFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("arrayParam");
        int[] integerArray = new int[0];
        ReflectionParamBuilderUtil3.buildParamFromObject(integerArray, objectParam, false);
        printParam(objectParam);
        int[] rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, int[].class);
        assertEquals(integerArray.length, rehydrated.length);
    }

    @Test
    public void testBuildArrayFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("arrayParam");
        int[] integerArray = new int[] { 1, 2, 3, 4, 5 };
        ReflectionParamBuilderUtil3.buildParamFromObject(integerArray, objectParam, false);
        printParam(objectParam);
        int[] rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, int[].class);
        for (int i = 0; i < integerArray.length; i++) {
            assertEquals(integerArray[i], rehydrated[i]);
        }
    }

    @Test
    public void testBuildMultidimensionalArrayFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("arrayParam");
        int[][] integerArray = new int[][] { { 1, 1 }, { 1, 2 }, { 2, 1 }, { 2, 2 } };
        ReflectionParamBuilderUtil3.buildParamFromObject(integerArray, objectParam, false);
        printParam(objectParam);
        int[][] rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, int[][].class);
        for (int i = 0; i < integerArray.length; i++) {
            for (int j = 0; j < integerArray[i].length; j++) {
                assertEquals(integerArray[i][j], rehydrated[i][j]);
            }
        }
    }

    @Test
    public void testBuildEmptyCollectionFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        Collection<Object> collection = new ArrayList<Object>();
        ReflectionParamBuilderUtil3.buildParamFromObject(collection, objectParam, false);
        printParam(objectParam);
        Collection<Object> rehydrated = ReflectionParamBuilderUtil3
                .buildObjectFromParams(objectParam, Collection.class);
        assertEquals(collection.size(), rehydrated.size());
    }

    @Test
    public void testBuildEmptyCollectionFromParamsWithReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        Collection<Object> collection = new ArrayList<Object>();
        ReflectionParamBuilderUtil3.buildParamFromObject(collection, objectParam);
        printParam(objectParam);
        Collection<Object> rehydrated = ReflectionParamBuilderUtil3
                .buildObjectFromParams(objectParam, Collection.class);
        assertEquals(collection.size(), rehydrated.size());
    }

    @Test
    public void testBuildCollectionWithSimpleObjectsFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        Collection<String> collection = new ArrayList<String>();
        collection.add("Listval 1");
        collection.add("Listval 2");
        collection.add(null);
        ReflectionParamBuilderUtil3.buildParamFromObject(collection, objectParam, false);
        printParam(objectParam);
        Collection<String> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, collection
                .getClass());
        assertEquals(collection.size(), rehydrated.size());
    }

    @Test
    public void testBuildCollectionWithSimpleObjectsFromParamsWithReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        Collection<String> collection = new ArrayList<String>();
        collection.add("Listval 1");
        collection.add("Listval 2");
        collection.add(null);
        ReflectionParamBuilderUtil3.buildParamFromObject(collection, objectParam);
        printParam(objectParam);
        Collection<String> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, collection
                .getClass());
        assertEquals(collection.size(), rehydrated.size());
    }

    @Test
    public void testBuildCollectionFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        Collection<MyInterface> collection = new ArrayList<MyInterface>();
        collection.add(new MyConcreteOne("Listval 1"));
        collection.add(new MyConcreteOne("Listval 2"));
        collection.add(new MyConcreteTwo());
        ReflectionParamBuilderUtil3.buildParamFromObject(collection, objectParam, false);
        printParam(objectParam);
        Collection<MyInterface> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, collection
                .getClass());
        assertEquals(collection.size(), rehydrated.size());
    }

    @Test
    public void testBuildCollectionFromParamsWithReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        Collection<MyInterface> collection = new ArrayList<MyInterface>();
        collection.add(new MyConcreteOne("Listval 1"));
        collection.add(new MyConcreteOne("Listval 2"));
        collection.add(new MyConcreteTwo());
        ReflectionParamBuilderUtil3.buildParamFromObject(collection, objectParam);
        printParam(objectParam);
        Collection<MyInterface> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, collection
                .getClass());
        assertEquals(collection.size(), rehydrated.size());
    }

    @Test
    public void testBuildEmptyMapFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("mapParam");
        Map<Object, Object> objectMap = new HashMap<Object, Object>();
        ReflectionParamBuilderUtil3.buildParamFromObject(objectMap, objectParam);
        printParam(objectParam);
        Map<Object, Object> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Map.class);
        assertEquals(objectMap.size(), rehydrated.size());
    }

    @Test
    public void testBuildEmptyMapFromParamsNoReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("mapParam");
        Map<Object, Object> objectMap = new HashMap<Object, Object>();
        ReflectionParamBuilderUtil3.buildParamFromObject(objectMap, objectParam, false);
        printParam(objectParam);
        Map<Object, Object> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Map.class);
        assertEquals(objectMap.size(), rehydrated.size());
    }

    @Test
    public void testBuildMapFromParams() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("mapParam");
        Map<MyConcreteOne, MyInterface> objectMap = new HashMap<MyConcreteOne, MyInterface>();
        MyConcreteOne key1 = new MyConcreteOne("This is key one");
        MyConcreteOne value1 = new MyConcreteOne("This is value one");

        MyConcreteOne key2 = new MyConcreteOne("This is key two");
        MyConcreteTwo value2 = new MyConcreteTwo();

        objectMap.put(key1, value1);
        objectMap.put(key2, value2);
        objectMap.put(new MyConcreteOne("Key pointing at null"), null);
        ReflectionParamBuilderUtil3.buildParamFromObject(objectMap, objectParam, false);
        printParam(objectParam);
        Map<MyConcreteOne, MyInterface> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam,
                Map.class);
        assertEquals(objectMap.size(), rehydrated.size());
        Iterator<MyConcreteOne> keyIterator = rehydrated.keySet().iterator();
        assertEquals(key1, keyIterator.next());
        assertEquals(value1, rehydrated.get(key1));
        assertEquals(key2, keyIterator.next());
        assertEquals(value2, rehydrated.get(key2));
    }

    @Test
    public void testBuildMapFromParamsWithReferences() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("mapParam");
        Map<MyConcreteOne, MyInterface> objectMap = new HashMap<MyConcreteOne, MyInterface>();
        MyConcreteOne key1 = new MyConcreteOne("This is key one");
        MyConcreteOne value1 = new MyConcreteOne("This is value one");

        MyConcreteOne key2 = new MyConcreteOne("This is key two");
        MyConcreteTwo value2 = new MyConcreteTwo();

        objectMap.put(key1, value1);
        objectMap.put(key2, value2);
        objectMap.put(new MyConcreteOne("Key pointing at null"), null);
        ReflectionParamBuilderUtil3.buildParamFromObject(objectMap, objectParam);
        printParam(objectParam);
        Map<MyConcreteOne, MyInterface> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam,
                Map.class);
        assertEquals(objectMap.size(), rehydrated.size());
        Iterator<MyConcreteOne> keyIterator = rehydrated.keySet().iterator();
        assertEquals(key1, keyIterator.next());
        assertEquals(value1, rehydrated.get(key1));
        assertEquals(key2, keyIterator.next());
        assertEquals(value2, rehydrated.get(key2));
    }

    @Test
    public void testBuildMapOfSets() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("mapParam");
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        Set<String> set_one = new HashSet<String>();
        set_one.add("hi");
        set_one.add("there");
        map.put("set_one", set_one);
        Set<String> set_two = new HashSet<String>();
        set_two.add("maps of lists");
        set_two.add("are tricky");
        map.put("set_two", set_two);
        ReflectionParamBuilderUtil3.buildParamFromObject(map, objectParam);

        Map<String, Set<String>> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Map.class);
        assertNotNull(rehydrated);
        assertEquals(map.size(), rehydrated.size());
        assertTrue(rehydrated.get("set_one") instanceof Set);
    }

    @Test
    public void testBuildMapOfMaps() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("mapParam");
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        Map<String, String> map_one = new HashMap<String, String>();
        map_one.put("hi", "there");
        map.put("map_one", map_one);
        Map<String, String> map_two = new HashMap<String, String>();
        map_two.put("maps of maps", "are also tricky");
        map.put("map_two", map_two);
        ReflectionParamBuilderUtil3.buildParamFromObject(map, objectParam);

        Map<String, Set<String>> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Map.class);
        assertNotNull(rehydrated);
        assertEquals(map.size(), rehydrated.size());
        assertTrue(rehydrated.get("map_one") instanceof Map);
    }

    @Test
    public void testBuildListOfLists() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        List<List<String>> list = new LinkedList<List<String>>();
        List<String> list_one = new ArrayList<String>();
        list_one.add("hi");
        list_one.add("there");
        list.add(list_one);
        List<String> list_two = new ArrayList<String>();
        list_two.add("lists of maps");
        list_two.add("are tricky");
        list.add(list_two);

        ReflectionParamBuilderUtil3.buildParamFromObject(list, objectParam);

        List<List<String>> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, List.class);
        assertNotNull(rehydrated);
        assertEquals(list.size(), rehydrated.size());
        assertTrue(rehydrated.get(0) instanceof Collection);
    }

    @Test
    public void testBuildListOfMaps() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("listParam");
        List<Map<String, String>> list = new LinkedList<Map<String, String>>();
        Map<String, String> map_one = new HashMap<String, String>();
        map_one.put("hi", "there");
        list.add(map_one);
        Map<String, String> map_two = new HashMap<String, String>();
        map_two.put("lists of maps", "are tricky");
        list.add(map_two);

        ReflectionParamBuilderUtil3.buildParamFromObject(list, objectParam);

        List<Map<String, String>> rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam,
                List.class);
        assertNotNull(rehydrated);
        assertEquals(list.size(), rehydrated.size());
        assertTrue(rehydrated.get(0) instanceof Map);
    }

    @Test
    public void testSubclassArray() throws Exception {
        MyInterface[] listArray = new MyInterface[] { new MyConcreteOne(), new MyConcreteTwo() };
        Param objectParam = new Param();
        objectParam.setName("listArrayParam");
        ReflectionParamBuilderUtil3.buildParamFromObject(listArray, objectParam, false);
        printParam(objectParam);
        MyInterface[] rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, listArray.getClass());
        assertEquals(listArray.length, rehydrated.length);
        for (int i = 0; i < rehydrated.length; i++) {
            assertEquals(listArray[i], rehydrated[i]);
        }
    }

    @Test
    public void testException() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("exceptionParam");
        Exception e = new Exception("Contains another exception", new Exception("Contained"));
        ReflectionParamBuilderUtil3.buildParamFromObject(e, objectParam);
        printParam(objectParam);
        Exception rehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Exception.class);
        assertEquals(e.getMessage(), rehydrated.getMessage());
        assertNotNull(rehydrated.getCause());
        assertEquals(e.getCause().getMessage(), rehydrated.getCause().getMessage());
    }

    @Test
    public void testCreateException() throws Exception {
        Param exceptionParam = new Param();
        exceptionParam.setName("java.lang.Exception");
        exceptionParam.addParam(createParam("detailMessage", "An exception was thrown"));
        Exception e = ReflectionParamBuilderUtil3.buildObjectFromParams(exceptionParam, Exception.class);
        e.printStackTrace();
    }

    @Test
    public void testPeriodFormat() throws Exception {
        Param objectParam = new Param();
        objectParam.setName("objectParam");
        Period p = new Period(525600, PeriodType.minutes());
        ReflectionParamBuilderUtil3.buildParamFromObject(p, objectParam);
        printParam(objectParam);
        Period newRehydrated = ReflectionParamBuilderUtil3.buildObjectFromParams(objectParam, Period.class);
        assertEquals(p, newRehydrated);
    }

    @Test
    public void underscoreXPathReferences() throws Exception {
        DoublePeriod p = new DoublePeriod();
        LocalDateTime now = new LocalDateTime();
        PeriodType periodType = PeriodType.forFields(new DurationFieldType[] { DurationFieldType.hours(),
                DurationFieldType.minutes() });
        int hours = (int) (Math.random() * 2);
        p._a = new Period(now, now.plusHours(1), periodType);
        p.b = new Period(now, now.plusHours(hours), periodType);
        Param param = new Param();
        param.setName("foo");
        ReflectionParamBuilderUtil3.buildParamFromObject(p, param, true);
        printParam(param);
        DoublePeriod p2 = ReflectionParamBuilderUtil3.buildObjectFromParams(param, DoublePeriod.class);
        assertEquals(p._a, p2._a);
        assertEquals(p.b, p2.b);
        assertEquals(p2._a.getPeriodType(), p2.b.getPeriodType());
    }

    private String printParam(Param objectParam) throws IOException, MarshalException, ValidationException {
        XMLContext xmlContext = new XMLContext();
        xmlContext.setProperty("org.exolab.castor.indent", true);
        Marshaller m = xmlContext.createMarshaller();
        StringWriter w = new StringWriter();
        m.setWriter(w);
        m.marshal(objectParam);
        String data = w.toString();
        System.out.println(data);
        return data;
    }

    private Param createParam(String name, String value) {
        Param p = new Param();
        p.setName(name);
        p.setValue(value);
        return p;
    }

    public interface MyInterface {
    }

    public class MyConcreteOne implements MyInterface {
        private String _myString = "My String";

        public MyConcreteOne() {
        }

        public MyConcreteOne(String myString) {
            _myString = myString;
        }

        public void setString(String myString) {
            _myString = myString;
        }

        public int hashCode() {
            return 12;
        }

        public boolean equals(Object o) {
            if (o instanceof MyConcreteOne) {
                return ((MyConcreteOne) o)._myString.equals(_myString);
            }
            return false;
        }
    }

    public class MyConcreteTwo implements MyInterface {
        public int hashCode() {
            return 13;
        }

        public boolean equals(Object o) {
            if (o instanceof MyConcreteTwo) {
                return true;
            }
            return false;
        }
    }

    static class DoublePeriod {
        Period _a;
        Period b;
    }

}
