//
//  force10-plugin Written by Greg Walters
//  Copyright (C) 2011, Contegix, LLC, www.contegix.com
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms version 2 of the GNU General Public License as
//  published by the Free Software Foundation. This program is distributed
//  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
//  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
//  PARTICULAR PURPOSE. See the GNU General Public License for more
//  details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
//  USA.
//
//  About Contegix:
//  Contegix provides high-level managed hosting solutions for enterprise 
//  applications and infrastructure.  The company delivers proactive, 
//  passionate support that is unparalleled in the industry. All Contegix 
//  solutions encompass supporting dedicated hardware and operating system 
//  management, deploying and configuring software, and offering complete  
//  licensing management. Contegix\u2019s award-winning service is delivered 
//  by a staff of Tier-3 engineers from its global headquarters in St. Louis, 
//  MO. Current clients and partners include Six Apart, ReadWriteWeb, VMware
//  and Atlassian. For additional information, visit www.contegix.com or call 
//  1(877) 426-6834.

package com.contegix.gregwalters.hyperic.hq.plugin.force10;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hyperic.hq.product.RuntimeDiscoverer;
import org.hyperic.hq.product.RuntimeResourceReport;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.ServerDetector;
import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.AIServerExtValue;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.util.config.ConfigResponse;

import org.hyperic.hq.autoinventory.ServerSignature;

import org.hyperic.snmp.SNMPClient;
import org.hyperic.snmp.SNMPException;
import org.hyperic.snmp.SNMPSession;
import org.hyperic.snmp.SNMPValue;

public class force10ServerDetector  extends ServerDetector implements AutoServerDetector {

        static final String NUMBER_OF_UNITS = "chNumStackUnits";
        static final String UNIT_DESCRIPTION = "chStackUnitDescription";
        static final String UNIT_NAME = "mib-2.1.5.0";
        private transient Log log =  LogFactory.getLog("force10ServerDetector");

	public List getServerResources(ConfigResponse config) throws PluginException {
		log.debug("Running server discovery using config: " + config.toProperties());
		List servers = new ArrayList();
		try {
			SNMPSession session = getSNMPSession(config);
			int count = (int) session.getSingleValue(NUMBER_OF_UNITS).toLong();
			log.debug("Number of units in stack: " + count);
			for ( int unit=1; unit <= count; unit++ ) {
				ServerResource server =  createServerResource("/" + unit);
				ConfigResponse productConfig = new ConfigResponse();
				ConfigResponse customProperties = new ConfigResponse();
				productConfig.setValue("unitNumber" , unit);

				try {
					server.setDescription(session.getSingleValue(UNIT_DESCRIPTION + "." + unit).toString());
					customProperties.setValue("chStackUnitNumber", session.getSingleValue("chStackUnitNumber." + 
														unit).toString());
					customProperties.setValue("chStackUnitModelID", session.getSingleValue("chStackUnitModelID." +
														unit).toString());
					customProperties.setValue("chStackUnitDescription", session.getSingleValue("chStackUnitDescription." +
														unit).toString());
					customProperties.setValue("chStackUnitCodeVersion", session.getSingleValue("chStackUnitCodeVersion." +
														unit).toString());
					customProperties.setValue("chStackUnitSerialNumber", session.getSingleValue("chStackUnitSerialNumber."+
														unit).toString());
					customProperties.setValue("chStackUnitMfgDate", session.getSingleValue("chStackUnitMfgDate." +
														unit).toString());
					customProperties.setValue("chStackUnitMacAddress", session.getSingleValue("chStackUnitMacAddress." +
														unit).toString());
				} catch (SNMPException e) {
					throw new SNMPException("Error getting SNMP value: " + e.getMessage(), e);
				}

				server.setProductConfig(productConfig);
				server.setCustomProperties(customProperties);
				server.setMeasurementConfig();
				server.setName(config.getValue("platform.fqdn") + " Stack Unit " + unit);
				
				log.debug("Adding Server: " + server.toString());
				servers.add(server);
			}
		} catch (SNMPException e) {
			throw new PluginException("Error getting SNMP value: " + e.getMessage(), e);
		}
		return servers;
	}

	public List discoverServices(ConfigResponse config) throws PluginException {
		log.debug("Discovering Services using config: " + config.toProperties());
		List services = new ArrayList();
		SNMPSession session = getSNMPSession(config);
		int unit = Integer.parseInt(config.getValue("unitNumber"));
		List names;
		try {
			names = session.getColumn("ifName");
		} catch (SNMPException e) {
			throw new PluginException("Error getting SNMP column: " + e.getMessage(), e);
		}
                for (int i=0; i<names.size(); i++) {
                        String portName = (String)names.get(i).toString();
                        if (portName.indexOf(" " + Integer.toString(unit-1) + "/") != -1) {
                                SNMPValue snmpVal = (SNMPValue)names.get(i);
                                String oid = snmpVal.getOID();
                                int idx = oid.lastIndexOf(".");
                                Integer index = Integer.parseInt(oid.substring(idx+1));
                                //ServiceResource service = createServiceResource("Interface");
                                ServiceResource service = new ServiceResource();
                                service.setType(this, "Interface");
                                service.setServiceName("Port " + index.toString());
                                ConfigResponse productConfig = new ConfigResponse();
                                productConfig.setValue("port", index);
                                //service.setProductConfig(productConfig);
                                setProductConfig(service, productConfig);
				ConfigResponse customProperties = new ConfigResponse();
				try {
					service.setDescription(session.getSingleValue("ifDescr." + index).toString());
				} catch (SNMPException e) {
					throw new PluginException("Error getting SNMP value: " + e.getMessage(), e);
				}
                                service.setMeasurementConfig();
                                service.setControlConfig();
				log.debug("Adding service: " + service.toString());
				services.add(service);
			}
		}
		return services;
	}

	public SNMPSession getSNMPSession(ConfigResponse config) throws PluginException {
		SNMPClient client = new SNMPClient();
		SNMPSession session;
		try {
			session = client.getSession(config);
		} catch (SNMPException e) {
			throw new PluginException("Error getting SNMP session: " + e.getMessage(), e);
		}
		return session;
	}
}
