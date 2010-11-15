package org.mdf.mockdata.capture;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.mdf.mockdata.MockDataManager;
import org.mdf.mockdata.ReflectionParamBuilderUtil3;
import org.mdf.mockdata.generated.Category;
import org.mdf.mockdata.generated.MockData;
import org.mdf.mockdata.generated.Param;
import org.mdf.mockdata.generated.Request;
import org.mdf.mockdata.generated.Response;
import org.mdf.mockdata.generated.Test;

public class CapturingConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

	private static final long serialVersionUID = 1L;
	private boolean _useXStreamReferences = false;
	private String _dumpDir;
	private boolean _validate = true;
	AtomicInteger count = new AtomicInteger(0);

	@Override
	public V get(Object key) {
		try {
			Test test = new Test();
			Request tdRequest = new Request();
			Response tdResponse = new Response();
			test.setRequest(tdRequest);
			test.setResponse(tdResponse);

			Param keyParam = new Param();
			keyParam.setName("key");
			test.getRequest().addParam(keyParam);
			ReflectionParamBuilderUtil3.buildParamFromObject(key, keyParam,
					_useXStreamReferences);
			V obj = super.get(key);
			if (obj != null) {

				Param p = new Param();
				p.setName("value");
				test.getResponse().addParam(p);
				
					
				
				ReflectionParamBuilderUtil3.buildParamFromObject(obj, p,
						_useXStreamReferences);
				if (ReflectionParamBuilderUtil3.isPrimitiveType(obj.getClass())
                        || obj.getClass().isPrimitive()) {
				Param implTypeParam = new Param();
				implTypeParam
						.setName(ReflectionParamBuilderUtil3.IMPLEMENTATION_TYPE_PARAM_NAME);
				implTypeParam.setValue(obj.getClass().getName());
				p.addParam(implTypeParam);
				}

			}

			MockData mockData = new MockData();
			Category category = new Category();
			category.setName("map");
			mockData.addCategory(category);
			category.addTest(test);

					count.addAndGet(1);
			
			String fileName = new StringBuilder(obj.getClass().getName())
					.append(count.getAndIncrement()).append(".get.xml").toString();
			String dumpDir = _dumpDir == null ? System
					.getProperty("orbitz.server.log.dir") : _dumpDir;
			MockDataManager.marshallTestData(mockData, dumpDir + File.separator
					+ fileName, _validate);
			return obj;

		} catch (Throwable t) {
			Logger.getLogger(CapturingConcurrentHashMap.class).warn(
					"Unable to capture mock data", t);
			return null;
		}

	}

	public boolean is_useXStreamReferences() {
		return _useXStreamReferences;
	}

	public void set_useXStreamReferences(boolean _useXStreamReferences) {
		this._useXStreamReferences = _useXStreamReferences;
	}

	public String get_dumpDir() {
		return _dumpDir;
	}

	public void set_dumpDir(String _dumpDir) {
		this._dumpDir = _dumpDir;
	}

	public boolean is_validate() {
		return _validate;
	}

	public void set_validate(boolean _validate) {
		this._validate = _validate;
	}

}
