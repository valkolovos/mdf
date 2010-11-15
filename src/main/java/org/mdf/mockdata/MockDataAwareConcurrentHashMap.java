package org.mdf.mockdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.mdf.mockdata.generated.Param;


public class MockDataAwareConcurrentHashMap<K, V> extends
		ConcurrentHashMap<K, V> {

	private static final long serialVersionUID = 1L;
	public MockDataManager mdm;

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		V value = null;
		Param keyParam = new Param();
		
		ArrayList<Param> requestParams = new ArrayList<Param>();
		try {
			ReflectionParamBuilderUtil3.buildParamFromObject(key, keyParam);
			if(keyParam.getName()==null)
				keyParam.setName("key");
			requestParams.add(keyParam);
			Param[] response = mdm.findResponse(requestParams);
			if (response.length == 0)
				value = super.get(key);
			else{
				
				
				
				value = (V) ReflectionParamBuilderUtil3.buildObjectFromParams(
						response[0], Serializable.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;

	}

	public MockDataAwareConcurrentHashMap(MockDataManager mdm) {
		super();
		this.mdm = mdm;
	};

}
