package org.yarnandtail.andhow;

import java.util.*;
import org.yarnandtail.andhow.api.*;
import org.yarnandtail.andhow.load.KeyValuePairLoader;
import org.yarnandtail.andhow.load.std.*;
import org.yarnandtail.andhow.name.CaseInsensitiveNaming;
import org.yarnandtail.andhow.property.StrProp;
import org.yarnandtail.andhow.util.TextUtil;

/**
 *
 * @author ericeverman
 */
public class StdConfig implements AndHowConfiguration {

	//A list of hardcoded values used by the StdFixedValueLoader
	private final List<PropertyValue> _fixedVals = new ArrayList();

	//A list of command line arguments
	private final List<String> _cmdLineArgs = new ArrayList();

	//Prop file on classpath
	private String classpathPropFilePathStr;	//mutually XOR
	private StrProp classpathPropFilePathProp;	//mutually XOR
	private boolean _missingClasspathPropFileAProblem = false;

	//Prop file on filesystem path
	private StrProp filesystemPropFilePathProp;
	private boolean _missingFilesystemPropFileAProblem = false;

	//System Properties
	private Properties systemProperties;

	//System Environment
	private Map<String, String> envProperties;
	
	NamingStrategy naming = new CaseInsensitiveNaming();
	
	public static StdConfig instance() {
		return new StdConfig();
	}

	/**
	 * Sets a fixed, non-configurable value for a Property.
	 *
	 * Property values set in this way use the FixedValueLoader to load values
	 * prior to any other loader. Since the first loaded value for a Property
	 * 'wins', this effectively fixes the value and makes it non-configurable.
	 *
	 * @param <T> The type of Property and value
	 * @param property The property to set a value for
	 * @param value The value to set.
	 * @return
	 */
	public <T> StdConfig addFixedValue(Property<T> property, T value) {

		if (property == null) {
			throw new IllegalArgumentException("The property cannot be null");
		}

		for (PropertyValue pv : _fixedVals) {
			if (property.equals(pv.getProperty())) {
				throw new IllegalArgumentException("A fixed value for this property has been assigned twice.");
			}
		}

		PropertyValue pv = new PropertyValue(property, value);
		_fixedVals.add(pv);

		return this;
	}

	/**
	 * Removes a PropertyValue from the list of fixed values.
	 *
	 * It is not an error to attempt to remove a property that is not in the
	 * current fixed value list.
	 *
	 * @param property A non-null property.
	 * @return
	 */
	public StdConfig removeFixedValue(Property<?> property) {

		if (property == null) {
			throw new IllegalArgumentException("The property cannot be null");
		}

		Iterator<PropertyValue> it = _fixedVals.iterator();
		while (it.hasNext()) {
			PropertyValue pv = it.next();
			if (property.equals(pv.getProperty())) {
				it.remove();
				break;
			}
		}

		return this;
	}

	/**
	 * Adds the command line arguments, keeping any previously added.
	 *
	 * @param commandLineArgs
	 * @return
	 */
	public StdConfig addCmdLineArgs(String[] commandLineArgs) {

		if (commandLineArgs != null && commandLineArgs.length > 0) {
			_cmdLineArgs.addAll(Arrays.asList(commandLineArgs));
		}

		return this;
	}

	/**
	 * Adds a command line argument in key=value form.
	 *
	 * If the value is null, only the key is added (ie its a flag).
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public StdConfig addCmdLineArg(String key, String value) {

		if (key == null) {
			throw new RuntimeException("The key cannot be null");
		}

		if (value != null) {
			_cmdLineArgs.add(key + KeyValuePairLoader.KVP_DELIMITER + value);
		} else {
			_cmdLineArgs.add(key);
		}

		return this;
	}

	/**
	 * Sets the classpath path to a properties file for the
	 * StdPropFileOnClasspathLoader to load.
	 *
	 * If no path is specified via either a String or StrProp, the path
	 * '/andhow.properties' is used.<br/>
	 *
	 * Paths should start with a forward slash and have packages delimited by
	 * forward slashes. If the file name contains a dot, the path <em>must</em>
	 * start with a forward slash.
	 *
	 * @param classpathPropFilePathString
	 * @return
	 */
	public StdConfig setClasspathPropFilePath(String classpathPropFilePathString) {

		classpathPropFilePathString = TextUtil.trimToNull(classpathPropFilePathString);

		if (classpathPropFilePathString != null && classpathPropFilePathProp != null) {
			throw new IllegalArgumentException("The property file classpath cannot "
					+ "be specified as both a String and StrProp");
		}

		if (classpathPropFilePathString != null && !classpathPropFilePathString.startsWith("/")
				&& (classpathPropFilePathString.endsWith(".properties") || classpathPropFilePathString.endsWith(".xml"))) {

			throw new IllegalArgumentException("The path to the property file on "
					+ "the classpath should start with a '/' if the filename contains a dot.");
		}
		this.classpathPropFilePathStr = classpathPropFilePathString;

		return this;
	}

	/**
	 * Sets the classpath path via a StrProp (a Property of String type) to a
	 * properties file for the StdPropFileOnClasspathLoader to load.
	 *
	 * If no path is specified via either a String or StrProp, the path
	 * '/andhow.properties' is used.<br/>
	 *
	 * Paths should start with a forward slash and have packages delimited by
	 * forward slashes. If the file name contains a dot, the path <em>must</em>
	 * start with a forward slash. Thus, it is good practice to add a validation
	 * rule to the StrProp used here to ensure it
	 * <code>mustStartWith("/")</code>.
	 *
	 * @param classpathPropFilePathProperty
	 * @return
	 */
	public StdConfig setClasspathPropFilePath(StrProp classpathPropFilePathProperty) {

		if (classpathPropFilePathStr != null && classpathPropFilePathProperty != null) {
			throw new IllegalArgumentException("The property file classpath cannot "
					+ "be specified as both a String and StrProp");
		}

		this.classpathPropFilePathProp = classpathPropFilePathProperty;

		return this;
	}

	/**
	 * If set, the properties file loaded by StdPropFileOnClasspathLoader must
	 * be found and a RuntimeException will be thrown if it is not found.
	 *
	 * This is not set by default, allowing the properties file to be optional.
	 *
	 * @return
	 */
	public StdConfig classpathPropertiesRequired() {
		_missingClasspathPropFileAProblem = true;
		return this;
	}

	/**
	 * If set, the properties file loaded by StdPropFileOnClasspathLoader is
	 * optional and will not throw an error if it is not found.
	 *
	 * This is set by default, so there is no need to explicitly call it.
	 *
	 * @return
	 */
	public StdConfig classpathPropertiesNotRequired() {
		_missingClasspathPropFileAProblem = false;
		return this;
	}

	/**
	 * Sets the filesystem path via a StrProp (a Property of String type) to a
	 * properties file for the StdPropFileOnFilesystemLoader to load.
	 *
	 * If no property is set to specify a path, or a property is set by has no
	 * value, this loader won't be used. If the property is specified but the
	 * specified file is missing, an error will be thrown based on the
	 * filesystemPropFileRequired flag.
	 *
	 * Paths should generally be absolute and correctly formed for the host
	 * environment.
	 *
	 * @param filesystemPropFilePath
	 * @return
	 */
	public StdConfig setFilesystemPropFilePath(StrProp filesystemPropFilePath) {
		this.filesystemPropFilePathProp = filesystemPropFilePath;
		return this;
	}

	/**
	 * If set, the properties file loaded by StdPropFileOnFilesystemLoader must
	 * be found and a RuntimeException will be thrown if it is not found.
	 *
	 * This is not set by default, allowing the properties file to be optional.
	 *
	 * @return
	 */
	public StdConfig filesystemPropFileRequired() {
		_missingFilesystemPropFileAProblem = true;
		return this;
	}

	/**
	 * If set, the properties file loaded by StdPropFileOnFilesystemLoader is
	 * optional and will not throw an error if it is not found.
	 *
	 * This is set by default, so there is no need to explicitly call it.
	 *
	 * @return
	 */
	public StdConfig filesystemPropFileNotRequired() {
		_missingFilesystemPropFileAProblem = false;
		return this;
	}

	/**
	 * Allows system properties to be overridden.
	 *
	 * @param properties
	 */
	public StdConfig setSystemProperties(Properties properties) {
		systemProperties = properties;
		return this;
	}

	/**
	 * Allows the System environment to be overridden.
	 *
	 * @param envProperties
	 * @return
	 */
	public StdConfig setEnvironmentProperties(Map<String, String> envProperties) {
		this.envProperties = envProperties;
		return this;
	}
	
	@Override
	public NamingStrategy getNamingStrategy() {
		return naming;
	}

	@Override
	public List<Loader> buildLoaders() {
		List<Loader> loaders = new ArrayList();

		StdFixedValueLoader fvl = new StdFixedValueLoader();
		fvl.setPropertyValues(_fixedVals);
		loaders.add(fvl);

		StdMainStringArgsLoader clal = new StdMainStringArgsLoader();
		clal.setKeyValuePairs(_cmdLineArgs);
		loaders.add(clal);

		StdSysPropLoader spl = new StdSysPropLoader();
		spl.setMap(systemProperties);
		loaders.add(spl);

		loaders.add(new StdJndiLoader());

		StdEnvVarLoader evl = new StdEnvVarLoader();
		evl.setMap(envProperties);
		loaders.add(evl);

		StdPropFileOnFilesystemLoader pfofsl = new StdPropFileOnFilesystemLoader();
		pfofsl.setFilePath(filesystemPropFilePathProp);
		pfofsl.setMissingFileAProblem(_missingFilesystemPropFileAProblem);
		loaders.add(pfofsl);

		StdPropFileOnClasspathLoader pfocpl = new StdPropFileOnClasspathLoader();
		pfocpl.setMissingFileAProblem(_missingClasspathPropFileAProblem);
		if (classpathPropFilePathStr != null) {
			pfocpl.setFilePath(classpathPropFilePathStr);
		} else if (classpathPropFilePathProp != null) {
			pfocpl.setFilePath(classpathPropFilePathProp);
		}

		loaders.add(pfocpl);

		return loaders;
	}

}
