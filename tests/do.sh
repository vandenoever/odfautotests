#!/usr/bin/env bash

# todo: rename .xml .oat for nicer recognition

xsltproc --stringparam mode odt createTextPropertiesTests.xsl createTextPropertiesTests.xsl | xmlstarlet fo > text-properties-odt.xml
xsltproc --stringparam mode ods createTextPropertiesTests.xsl createTextPropertiesTests.xsl | xmlstarlet fo > text-properties-ods.xml
xsltproc --stringparam mode odp createTextPropertiesTests.xsl createTextPropertiesTests.xsl | xmlstarlet fo > text-properties-odp.xml
xsltproc --stringparam mode odt createParagraphPropertiesTests.xsl createParagraphPropertiesTests.xsl | xmlstarlet fo > paragraph-properties-odt.xml
xsltproc --stringparam mode ods createParagraphPropertiesTests.xsl createParagraphPropertiesTests.xsl | xmlstarlet fo > paragraph-properties-ods.xml
xsltproc --stringparam mode odp createParagraphPropertiesTests.xsl createParagraphPropertiesTests.xsl | xmlstarlet fo > paragraph-properties-odp.xml

xsltproc createTextChangeTrackingTests.xsl createTextChangeTrackingTests.xsl | xmlstarlet fo > textchangetrackingtests.xml
