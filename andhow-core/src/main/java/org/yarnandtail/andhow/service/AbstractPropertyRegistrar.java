package org.yarnandtail.andhow.service;

import java.util.Collections;
import java.util.List;


/**
 * Provides a minimal implementation of {@code PropertyRegistrar} to simplify
 * code that must be generated.
 * 
 * <h3>Property registration background</h3>
 * At compile time, the AndHowCompileProcessor (an annotation Processor), reads
 * user classes and generates a PropertyRegistrar instance for each root class
 * (non-inner class) that contains an AndHow {@code Property}.
 * Matching service files are also generated in the "META-INF/services/"
 * directory so the {@code PropertyRegistrar} instances can be discovered
 * through the {@code java.util.ServiceLoader} mechanism.
 * <p>
 * At run time, the {@code PropertyRegistrarLoader} discovers all
 * {@code PropertyRegistrar} instances.
 * Each {@code PropertyRegistrar} creates a {@code PropertyRegistrationList}
 * instance with a {@code PropertyRegistration} for each {@code Property}
 * present in the jar.
 * 
 * @author ericeverman
 */
public abstract class AbstractPropertyRegistrar implements PropertyRegistrar {
	
	@Override
	public List<PropertyRegistration> getRegistrationList() {
		PropertyRegistrationList list = new PropertyRegistrationList(getRootCanonicalName());
		addPropertyRegistrations(list);
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * Can be used by subclasses to build the property registration list.
	 * <p>
	 * This method will be called by {@code getRegistrationList} with an empty
	 * list.  Implementers can then add each registration to the list.
	 * 
	 * @param list The list to add {@code PropertyRegistrations} to.  For
	 * consistency, registrations should be added in the order they were encoded
	 * in the {@code PropertyRegistrar} instance.
	 */
	public abstract void addPropertyRegistrations(PropertyRegistrationList list);
	
}
