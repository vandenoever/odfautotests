#! /usr/bin/env bash
rm -r out/*
mkdir -p out/v1.1 out/v1.2 out/v1.2-part1 out/v1.2-part2 out/v1.2-part3
unzip -q v1.1/OpenDocument-v1.1.odt -d out/v1.1
unzip -q v1.2/OpenDocument-v1.2.odt -d out/v1.2
unzip -q v1.2/OpenDocument-v1.2-part1.odt -d out/v1.2-part1
unzip -q v1.2/OpenDocument-v1.2-part2.odt -d out/v1.2-part2
unzip -q v1.2/OpenDocument-v1.2-part3.odt -d out/v1.2-part3

cp spec.css out

# remove DTD so XSLT process will load the document
perl -pi -e 's#<!DOCTYPE math:math PUBLIC "-//OpenOffice.org//DTD Modified W3C MathML 1.01//EN" "math.dtd">##' out/*/*/content.xml

xslt2 odt2html.xsl out/v1.1/content.xml
