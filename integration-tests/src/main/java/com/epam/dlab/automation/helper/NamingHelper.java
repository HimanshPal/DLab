/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.automation.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.epam.dlab.automation.aws.AmazonHelper;

public class NamingHelper {
    private static AtomicInteger idCounter = new AtomicInteger(0);
    
    private static String serviceBaseName;
    private static String ssnURL;
    private static String ssnIp;
    private static String ssnToken;
    
    public static String getServiceBaseName() {
    	return serviceBaseName;
    }
    
    public static void setServiceBaseName(String serviceBaseName) {
    	if (NamingHelper.serviceBaseName != null) {
    		throw new IllegalArgumentException("Field serviceBaseName already has a value");
    	}
    	NamingHelper.serviceBaseName = serviceBaseName;
    }
    
    public static String getSsnURL() {
    	return ssnURL;
    }
    
    public static void setSsnURL(String ssnURL) {
    	if (NamingHelper.ssnURL != null) {
    		throw new IllegalArgumentException("Field ssnURL already has a value");
    	}
    	NamingHelper.ssnURL = ssnURL;
    }

    public static String getSsnName() {
    	return serviceBaseName + "-ssn";
    }
    
    public static String getSsnIp() {
    	return ssnIp;
    }
    
    public static void setSsnIp(String ssnIp) {
    	if (NamingHelper.ssnIp != null) {
    		throw new IllegalArgumentException("Field ssnIp already has a value");
    	}
    	NamingHelper.ssnIp = ssnIp;
    }

    public static String getSsnToken() {
    	return ssnToken;
    }
    
    public static void setSsnToken(String ssnToken) {
    	if (NamingHelper.ssnToken != null) {
    		throw new IllegalArgumentException("Field ssnToken already has a value");
    	}
    	NamingHelper.ssnToken = ssnToken;
    }
    
    public static String getEdgeName() {
    	return String.join("-", serviceBaseName, ConfigPropertyValue.getUsernameSimple(), "edge");
    }
    
    public static String getNotebookInstanceName(String notebookName) {
    	return String.join("-", serviceBaseName, ConfigPropertyValue.getUsernameSimple(), "nb", notebookName);
    }
    
    public static String getEmrInstanceName(String notebookName, String emrName) {
    	return String.join("-", serviceBaseName, ConfigPropertyValue.getUsernameSimple(), "emr", notebookName, emrName);
    }

    public static String getNotebookContainerName(String notebookName, String action) {
    	return String.join("_", ConfigPropertyValue.getUsernameSimple(), action, "exploratory", notebookName);
    }
    
    public static String getEmrContainerName(String emrName, String action) {
    	return String.join("_", ConfigPropertyValue.getUsernameSimple(), action, "computational", emrName);
    }
    
    
    public static String generateRandomValue() {
        SimpleDateFormat df = new SimpleDateFormat("YYYYMMddhmmss");
        return String.join("_",  "ITest", df.format(new Date()), String.valueOf(idCounter.incrementAndGet()));
    }

    public static String generateRandomValue(String notebokTemplateName) {
        SimpleDateFormat df = new SimpleDateFormat("YYYYMMddhmmss");
        return String.join("_",  "ITest", notebokTemplateName, df.format(new Date()), String.valueOf(idCounter.incrementAndGet()));
    }
    
    public static String getSelfServiceURL(String path) {
        return ssnURL + path;
    }
    
    public static String getBucketName() {
    	return String.format("%s-%s-bucket", serviceBaseName, ConfigPropertyValue.getUsernameSimple()).replace('_', '-').toLowerCase();
    }
    
    public static String getEmrClusterName(String emrName) throws Exception {
        Instance instance = AmazonHelper.getInstance(emrName);
        for (Tag tag : instance.getTags()) {
			if (tag.getKey().equals("Name")) {
		        return tag.getValue();
			}
		}
        throw new Exception("Could not detect cluster name for EMR " + emrName);
    }
}
