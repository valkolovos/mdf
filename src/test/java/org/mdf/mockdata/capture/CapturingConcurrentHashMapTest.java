package org.mdf.mockdata.capture;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mdf.mockdata.ComplexClass;
import org.mdf.mockdata.ComplexClass2;
import org.testng.annotations.Test;

public class CapturingConcurrentHashMapTest {

	@Test
	public void testGetArrayWithMockData() throws Exception {
		
			CapturingConcurrentHashMap<Serializable, Serializable> map = new CapturingConcurrentHashMap<Serializable, Serializable>();
			map.set_dumpDir(System.getProperty("java.io.tmpdir"));

			Long[] array = new Long[] { 1l, 2l };
			Long[] array2 = new Long[] { 3l, 4l };
			map.put(array, array2);

			Long[] house = (Long[]) map.get(array);
			assertEquals(house[0], array2[0]);
			
			File dumpFile = new File(System.getProperty("java.io.tmpdir"), "[Ljava.lang.Long;1.get.xml");
			assertTrue(dumpFile.exists());
			dumpFile.delete();


	}

	@Test
	public void testGetCaptureWithString() throws Exception {
		
			CapturingConcurrentHashMap<Serializable, Serializable> map = new CapturingConcurrentHashMap<Serializable, Serializable>();
			map.set_dumpDir(System.getProperty("java.io.tmpdir"));

			map.put("foo", "house");

			String house = (String) map.get("foo");
			assertEquals(house, "house");
			File dumpFile = new File(System.getProperty("java.io.tmpdir"), "java.lang.String1.get.xml");
			assertTrue(dumpFile.exists());
			dumpFile.delete();

		
	}

	@Test
	public void testGetCaptureWithComplexClass() throws Exception {
		CapturingConcurrentHashMap<Serializable, Serializable> map = new CapturingConcurrentHashMap<Serializable, Serializable>();
		map.set_dumpDir(System.getProperty("java.io.tmpdir"));
		Map<String, ComplexClass2> m = new HashMap<String, ComplexClass2>();
		ComplexClass foo = new ComplexClass(m, true);
		ComplexClass2 frond = new ComplexClass2("krakatoa");
		map.put(frond, foo);

		ComplexClass house = (ComplexClass) map.get(frond);
		assertTrue(foo.equals(house));
		File dumpFile = new File(System.getProperty("java.io.tmpdir"), "org.mdf.mockdata.ComplexClass1.get.xml");
		assertTrue(dumpFile.exists());
		dumpFile.delete();

	}

}