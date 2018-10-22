package org.yarnandtail.andhow.example.restclient;

import org.yarnandtail.andhow.property.FlagProp;
import org.yarnandtail.andhow.property.IntProp;
import org.yarnandtail.andhow.property.StrProp;

/**
 *
 * @author eeverman
 */
public interface SampleRestClientGroup {
	
	StrProp CLASSPATH_PROP_FILE = StrProp.builder().desc("Classpath location of a properties file w/ props").build();
	StrProp APP_NAME = StrProp.builder().aliasIn("app.name").aliasIn("app_name").build();
	StrProp REST_HOST = StrProp.builder().mustMatchRegex(".*\\.usgs\\.gov") .mustBeNonNull().build();
	IntProp REST_PORT = IntProp.builder().mustBeNonNull().mustBeGreaterThanOrEqualTo(80).mustBeLessThan(10000).build();
	StrProp REST_SERVICE_NAME = StrProp.builder().defaultValue("query/").mustEndWith("/").build();
	StrProp AUTH_KEY = StrProp.builder().mustBeNonNull().build();
	IntProp RETRY_COUNT = IntProp.builder().defaultValue(2).build();
	FlagProp REQUEST_META_DATA = FlagProp.builder().defaultValue(true).build();
	FlagProp REQUEST_SUMMARY_DATA = FlagProp.builder().build();
}
