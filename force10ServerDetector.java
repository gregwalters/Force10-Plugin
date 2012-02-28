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

        public AIPlatformValue discoverResources(AIPlatformValue aiplatform, ConfigResponse platformConfig)
            throws PluginException {
		
		SNMPClient client = new SNMPClient();
		SNMPSession session;

		log.debug("Running discover using config: " + platformConfig.toProperties());
		try {
			session = client.getSession(platformConfig);
			int count = (int) session.getSingleValue(NUMBER_OF_UNITS).toLong();
			log.debug("Number of units in stack: " + count);
			for ( int unit=1; unit <= count; unit++ ) {
				AIServerExtValue server = new AIServerExtValue();
				//ServerResource server = createServerResource("/" + unit);
				try {
					ConfigResponse measurementConfig = new ConfigResponse();
					ConfigResponse productConfig = new ConfigResponse();
					ConfigResponse controlConfig = new ConfigResponse();
					measurementConfig.setValue("unitNumber" , unit);
					try {
						server.setDescription(session.getSingleValue(UNIT_DESCRIPTION + "." + unit).toString());
					} catch (SNMPException e) {
						throw new SNMPException("Error getting SNMP value: " + e.getMessage(), e);
					}
					try {
						server.setProductConfig(productConfig.encode());
						server.setMeasurementConfig(measurementConfig.encode());
						server.setControlConfig(controlConfig.encode());
					} catch (Exception e) {
						throw new PluginException("Unable to generate product config.");
					}
					server.setServerTypeName("Stack Unit");
					server.setName(getPlatformName() + " Unit " + unit);
					server.setInstallPath("/" + unit);
					server.setAutoinventoryIdentifier(getPlatformName() + " Unit " + unit);
					server.setCTime(new Long(System.currentTimeMillis()));
					log.debug("Adding AIServerValue: " + server.toString());
					aiplatform.addAIServerValue(server);
				} catch (SNMPException e) {
					throw new SNMPException("Error getting SNMP value: " + e.getMessage(), e);
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (SNMPException e) {
			throw new PluginException("Error getting SNMP value: " + e.getMessage(), e);
		}

		return aiplatform;
	}

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
				productConfig.setValue("unitNumber" , unit);

				try {
					server.setDescription(session.getSingleValue(UNIT_DESCRIPTION + "." + unit).toString());
					getServiceResources(server, session, unit);
				} catch (SNMPException e) {
					throw new SNMPException("Error getting SNMP value: " + e.getMessage(), e);
				}

				server.setProductConfig(productConfig);
				server.setMeasurementConfig();
				
				log.debug("Adding Server: " + server.toString());
				servers.add(server);
			}
		} catch (SNMPException e) {
			throw new PluginException("Error getting SNMP value: " + e.getMessage(), e);
		}
		return servers;
	}

	public void getServiceResources(ServerResource server, SNMPSession session, int unit) throws PluginException {
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
				service.setType("Interface");
				service.setServiceName("Port " + index.toString());
				ConfigResponse productConfig = new ConfigResponse();
				productConfig.setValue("port", index);
				//service.setProductConfig(productConfig);
				setProductConfig(service, productConfig);
				service.setMeasurementConfig();
				service.setControlConfig();
				server.addService(service);
				log.debug("Adding service: " + service.toString());
			}
		}
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
