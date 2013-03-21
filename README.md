CILsearch
=========

a prototype for making any IMAP mailbox searchable by using scalable cloud services


Introduction
------------

This work originated from a student project in the eOrganization research group of the Karlsruhe Institute of Technology (KIT) in the summer of 2012. What is it good for? This software downloads an entire IMAP account (incl. some common attachments like Word docs, PDF,... ) and feeds it to a cloud search engine. A frontend can then be used to search this mailbox by the power of the search engines. Supported search engines are Amazon's CloudSearch and ElasticSearch.

Usage Notes
-----------

We only uploaded the source code without any dependencies. You can basically clone this repository into Eclipse and add the libraries listed below. As it is a GWT project, you might want to add GWT plugin support to your Eclipse. After setting up everything, run the ANT build and try running the code in Eclipse's development server.

### Dependencies ###

Those go into the /war/lib directory of the project:    
GWT SDK (gwt-servlet.jar) 
AWS Java SDK and dependencies   
Jersey (JAX-RS)   
JavaMail (and dependencies)   
Guava GWT   
ElasticSearch Java API (with lucene dependency)   
PDFBox   
POI   

These go to the lib directory:   
/lib/gwt-dev.jar   
/lib/gwt-user.jar   
/lib/validation-api-1.0.0.GA.jar   
/lib/validation-api-1.0.0.GA-sources.jar   


### Further settings ###

GlobalPropertyStore.java: here you set the current search engine provider (default: CloudSearch)   
AutoCloudSearchInstance.java: this file helps you set up a AWS CloudSearch domain. you need to set your awsAccountId here   

AwsCredentials.properties: AWS Credentials   
AwsData.properties: AWS CloudSearch domain data   
ESData.properties: ElasticSearch cluster data   
IMAPAccountCredentials.properties: IMAP Account data   


Read this, too
--------------
We would like to thank Hans-Joerg Happel and Thomas King from audriga GmbH for their support and guidance while working on this project.


License
-------
Copyright (c) 2013, Jan-Philipp Hofste, Fabian Jost, Peter Natterer, Oliver Röss, http://github.com/cloudyday/CILsearch

Usage of the works is permitted provided that this instrument is retained with the works, so that any entity that uses the works is notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.

