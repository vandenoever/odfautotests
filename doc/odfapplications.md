# ODF applications

## ODF versions
So far, three versions of the ODF standard have been published: 1.0, 1.1 and 1.2. 

(Is a valid 1.0 also a valid 1.1?)

A valid ODF 1.2 file

ODF file types:

Text

Spreadsheet

Drawing

Presentation

Chart

Image

odt, ott, odg, otg, odp, otp, ods, ots, odc, otc, odi, oti, odf, odm, oth

ODF 1.0 defines a Relax NG schema in two flavors: normal and strict. The strict version puts some limitations on the output of the file.

Documents that conform to the OpenDocument specification MAY contain elements and attributes not specified within the OpenDocument schema. These must be defined in a foreign namespace.

Conforming implementation must read a document that is valid against the OpenDocument schema if all foreign elements and attributes are removed before validation takes place, or must write documents that are valid against the OpenDocument schema if all foreign elements and attributes are removed before validation takes place.

Since ODF 1.2 ODF defines conforming producers, conforming consumers, extended conforming producers and extended conforming consumers.



OpenDocument document
OpenDocument extended document




office:process-content


# Validating ODF documents

The contents of the XML files with ODF documents are described by a Relax NG specification. The XML files should pass this specification after the elements and attributes that are not part of the specification have been removed.
