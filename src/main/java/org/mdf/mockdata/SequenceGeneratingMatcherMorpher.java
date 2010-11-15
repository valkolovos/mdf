package org.mdf.mockdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.mdf.mockdata.generated.Param;


/**
 * {@link ParameterMatcher} and {@link ParameterMorpher} that handles sequence
 * generation for Oracle. This class is initialized values that map the
 * parameter name to a value that can be referenced in a subsequent parameter.
 * For example, the SQL
 * 
 * <pre>
 * SELECT SEQUENCE_NAME.nextval
 * </pre>
 * 
 * might be mapped this way:
 * 
 * <pre>
 *   &lt;parameterMorpher class="com.orbitz.servicetests.mockdata.SequenceGeneratingMatcherMorpher"&gt;
 *     &lt;param name=&quot;SEQUENCE_NAME.nextval&quot; value=&quot;SEQUENCE_NAME&quot;/&gt;
 *   &lt;/parameterMorpher&gt;
 * </pre>
 * 
 * and referenced in the data file for morphing this way:
 * 
 * <pre>
 *  &lt;category&gt;
 *    &lt;param name="sqlSnippet" value="SELECT SEQUENCE_NAME.nextval FROM DUAL"/&gt;
 *    &lt;test&gt;
 *      &lt;request/&gt;
 *      &lt;response&gt;
 *        &lt;param name=&quot;row&quot;&gt;
 *          &lt;param name=&quot;SEQUENCE_NAME.nextval&quot;/&gt;
 *        &lt;/param&gt;
 *      &lt;/response&gt;
 *    &lt;/test&gt;
 *  &lt;/category&gt;
 * </pre>
 * 
 * In addition for matching, this class provides simple matching on the last
 * value generated. For example, if it was necessary to match on the resulting
 * of the above configuration, the configuration would look like this:
 * 
 * <pre>
 *   &lt;!-- using the ref attribute of parameterMorpher, you can reference an existing matcher --&gt;
 *   &lt;parameterMatcher id="sequenceMatcherMorpher" class="com.orbitz.servicetests.mockdata.SequenceGeneratingMatcherMorpher"&gt;
 *     &lt;param name="SEQUENCE_NAME.nextval" value="SEQUENCE_NAME"/&gt;
 *   &lt;/parameterMatcher&gt;
 *   &lt;parameterMorpher ref="sequenceMatcherMorpher"/&gt;
 *   &lt;test&gt;
 *     &lt;request&gt;
 *       &lt;param name=&quot;1&quot; value=&quot;SEQUENCE_NAME&quot;/&gt;
 *     &lt;/request&gt;
 *     &lt;response&gt;...&lt;/response&gt;
 *   &lt;/test&gt;
 * </pre>
 * 
 * This will match as long as the parameter passed in is the same as the last
 * generated sequence number. <br/>
 * <br/>
 * <b>New with version 3.5</b><br/>
 * The default column name of the row when morphing is "nextval". To override
 * that value (for use in something like a CallableStatement), give a value in
 * the mock data. For example:
 * 
 * <pre>
 *    &lt;response&gt;
 *      &lt;param name=&quot;row&quot;&gt;
 *        &lt;param name=&quot;SEQUENCE_NAME.nextval&quot; value=&quot;5&quot;/&gt;
 *      &lt;/param&gt;
 *    &lt;/response&gt;
 * </pre>
 * 
 * Initialization parameters are:<br/>
 * (parameter name) = (parameter value) - map the parameter name to a value that
 * can be referenced in a subsequent parameter<br/>
 * At least one is required
 */
public class SequenceGeneratingMatcherMorpher implements
		DelegatingParameterMatcher, ParameterMorpher {

	private Map<String, AtomicLong> _sequence = new HashMap<String, AtomicLong>();
	private Map<String, String> _sequenceNames = new HashMap<String, String>();
	private ParameterMatcher _delegateParameterMatcher = new DefaultParameterMatcher();

	public SequenceGeneratingMatcherMorpher() {
	}

	/**
	 * Constructs a new SequenceGeneratingMatcherMorpher with a
	 * {@link DefaultParameterMatcher} as its delegating parameter matcher
	 * 
	 * @param paramName
	 *            a Map<String,String> of values that map the parameter name to
	 *            a value that can be referenced in a subsequent parameter
	 * @deprecated - Initialize using mock data file and initialization
	 *             parameters instead
	 */
	public SequenceGeneratingMatcherMorpher(Map<String, String> paramName) {
		this(paramName, new DefaultParameterMatcher());
	}

	/**
	 * Constructs a new SequenceGeneratingMatcherMorpher with specified
	 * delegating parameter matcher
	 * 
	 * @param paramName
	 *            a Map<String,String> of values that map the parameter name to
	 *            a value that can be referenced in a subsequent parameter
	 * @param delegateParameterMatcher
	 *            the parameter matcher to delegate additional matching to
	 * @deprecated - Initialize using mock data file and initialization
	 *             parameters instead
	 */
	public SequenceGeneratingMatcherMorpher(Map<String, String> paramName,
			ParameterMatcher delegateParameterMatcher) {
		_sequenceNames = paramName;
		_delegateParameterMatcher = delegateParameterMatcher;
		for (String param : paramName.keySet()) {
			_sequence.put(param.toLowerCase(), new AtomicLong());
		}
	}

	public void setInitParams(Param... initParams) {
		for (Param p : initParams) {
			int initValue = 0;
			_sequenceNames.put(p.getName(), p.getValue());
			Param[] childParams = p.getParam();
			if (childParams.length != 0) {
				Param childParam = childParams[0];
				initValue=Integer.valueOf(childParam.getValue());
				
			}

			_sequence.put(p.getName().toLowerCase(), new AtomicLong(initValue));
		}
	}

	public void setDelegateParameterMatcher(
			ParameterMatcher delegateParameterMatcher) {
		_delegateParameterMatcher = delegateParameterMatcher;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canMorphParameter(String categoryName, String parameterName) {
		if (parameterName == null) {
			return false;
		}
		return _sequence.containsKey(parameterName.toLowerCase())
				|| _sequenceNames.containsValue(parameterName);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Param> morphParameter(Param param) throws Exception {
		List<Param> result = new ArrayList<Param>(1);
		if (_sequence.containsKey(param.getName().toLowerCase())) {
			Param resultParam = new Param();
			String resultParamName = "nextval";
			if (param.getValue() != null) {
				resultParamName = param.getValue();
			}
			resultParam.setName(resultParamName);
			resultParam.setValue(new Long(_sequence.get(
					param.getName().toLowerCase()).incrementAndGet())
					.toString());
			result.add(resultParam);
		} else {
			Param resultParam = new Param();
			resultParam.setName(param.getValue());
			resultParam.setValue(new Long(
					getLastSequenceForKey(param.getName())).toString());
			result.add(resultParam);
		}
		return result;
	}

	protected long getLastSequenceForKey(String key) {
		String sequenceKey = null;
		for (Map.Entry<String, String> entry : _sequenceNames.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(key)) {
				sequenceKey = entry.getKey().toLowerCase();
				break;
			}
		}
		AtomicLong currentSequenceVal = _sequence.get(sequenceKey);
		long lastSequence = -1;
		if (currentSequenceVal != null) {
			lastSequence = currentSequenceVal.get();
		}
		return lastSequence;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean paramsMatch(List<Param> requestParams, Param[] testParams) {
		Param[] clonedTestParams = testParams.clone();
		for (int i = 0; i < clonedTestParams.length; i++) {
			Param p = clonedTestParams[i];
			if (canMorphParameter(null, p.getValue())) {
				p = new Param();
				p.setName(clonedTestParams[i].getName());
				p.setValue(clonedTestParams[i].getValue());
				p.setValue(new Long(getLastSequenceForKey(p.getValue()))
						.toString());
				clonedTestParams[i] = p;
			}
		}
		return _delegateParameterMatcher.paramsMatch(requestParams,
				clonedTestParams);
	}

}
