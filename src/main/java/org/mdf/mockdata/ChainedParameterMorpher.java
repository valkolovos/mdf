package org.mdf.mockdata;

/**
 * A ChainedParameterMorpher returns parameters that may need additional
 * morphing. The ChainedParameterMorpher interface is a marker interface to
 * indicate to the MockDataManager that further processing is necessary.
 */
public interface ChainedParameterMorpher extends ParameterMorpher {

}
