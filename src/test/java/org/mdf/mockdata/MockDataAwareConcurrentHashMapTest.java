package org.mdf.mockdata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mdf.mockdata.MockDataAwareConcurrentHashMap;
import org.mdf.mockdata.MockDataManager;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


public class MockDataAwareConcurrentHashMapTest {

	@Test
	public void testGetWithMockData() throws Exception {
		try{
		MockDataManager manager = new MockDataManager(
				"org/mdf/mockdata/MockDataAwareConcurrentHashMapTestData.xml");
		MockDataAwareConcurrentHashMap<Serializable, Serializable> map = new MockDataAwareConcurrentHashMap<Serializable, Serializable>(
				manager);
		map.put("foo", "house");
		Long[] array = (Long[]) map.get(new Long[]{1l,2l});
		String house = (String)map.get("foo");
		assertEquals(1,array[0].intValue());
		assertEquals(2,array[1].intValue());
		assertEquals(3,array[2].intValue());
		assertEquals("house",house);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}
	
	@Test
	public void testGetWithCapturedData() throws Exception {
		try{
		MockDataManager manager = new MockDataManager(
				"org/mdf/mockdata/MockDataAwareMapTestData.xml");
		MockDataAwareConcurrentHashMap<Serializable, Serializable> map = new MockDataAwareConcurrentHashMap<Serializable, Serializable>(
				manager);
		Long[] array = new Long[] {1l,2l};
		Long[] house = (Long[])map.get(array);
				assertTrue(house[0].equals(3l));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}
	
	@Test
	public void testGetString() throws Exception {
		try{
		MockDataManager manager = new MockDataManager(
				"org/mdf/mockdata/MockDataAwareMapTestData.xml");
		MockDataAwareConcurrentHashMap<Serializable, Serializable> map = new MockDataAwareConcurrentHashMap<Serializable, Serializable>(
				manager);
		String house = (String)map.get("foo");
				assertEquals(house,"house");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}
	
	
	@Test
	public void testGetComplexClass() throws Exception {
		try{
		MockDataManager manager = new MockDataManager(
				"org/mdf/mockdata/MockDataAwareMapTestData.xml");
		MockDataAwareConcurrentHashMap<Serializable, Serializable> map = new MockDataAwareConcurrentHashMap<Serializable, Serializable>(
				manager);
		Map<String,ComplexClass2> m = new HashMap<String,ComplexClass2>();
		ComplexClass foo = new ComplexClass(m,true);
		ComplexClass2 frond = new ComplexClass2("krakatoa");
		ComplexClass house = (ComplexClass)map.get(frond);
				assertTrue(house.equals(foo));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}

}
