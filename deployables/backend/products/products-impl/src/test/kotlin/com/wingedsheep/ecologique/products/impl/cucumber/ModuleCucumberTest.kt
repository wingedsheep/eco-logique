package com.wingedsheep.ecologique.products.impl.cucumber

import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue", value = "com.wingedsheep.ecologique.products.impl.cucumber")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, html:build/reports/cucumber/module-cucumber.html")
class ModuleCucumberTest
