package org.mdf.mockdata;

/**
 * A DeferredParameterMorpher will be executed after all other morphers have executed. This is useful
 * if the morpher can have children that can / should / need to be morphed before the morpher applies.
 * 
 * @since 3.12
 */
public interface DeferredParameterMorpher extends ParameterMorpher {

}
