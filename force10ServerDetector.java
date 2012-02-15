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

package com.contegix.hyperic.force10.plugin;

import java.util.ArrayList;
import java.util.List;

import org.hyperic.hq.product.AutoServerDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerDetector;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.util.config.ConfigResponse;

import org.hyperic.snmp.SNMPClient;
import org.hyperic.snmp.SNMPException;
import org.hyperic.snmp.SNMPSession;
import org.hyperic.snmp.SNMPValue;

public class force10ServerDetector  extends ServerDetector implements AutoServerDetector {
	
	static final String NUMBER_OF_UNITS = "chNumStackUnits";
	static final String UNIT_DESCRIPTION = "chStackUnitDescription";
	static final String UNIT_NAME = "mib-2.1.5.0"

	public List getServerResources(ConfigResponse platformConfig) throws PluginException {
		List servers = new ArrayList();
		private SNMPClient client = new SNMPClient();
		SNMPSession session;
		session = client.getSession(config);
		int count = session.getSingleValue(NUMBER_OF_UNITS).toLong().intValue();

		for ( int unit=1; unit <= count; unit++ ) {
			ServerResource server = createServerResource(session.getSingleValue(UNIT_NAME).toString() + ":" + unit);
			ConfigResponse productConfig = new ConfigResponse();
			productConfig.setValue("unitNumber" , unit);
			productConfig.setValue("Description", session.getSingleValue(UNIT_DESCRIPTION).toString());
			server.setProductConfig(productConfig);
			servers.add(server);
		}
		return servers;
	}
}
