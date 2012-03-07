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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hyperic.hq.product.SNMPMeasurementPlugin;

import org.hyperic.hq.product.Metric;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.product.MetricNotFoundException;
import org.hyperic.hq.product.MetricUnreachableException;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.measurement.MeasurementConstants;

public class force10MeasurementPlugin extends SNMPMeasurementPlugin {

        private transient Log log =  LogFactory.getLog("force10MeasurementPlugin");

	public MetricValue getValue(Metric metric) throws MetricUnreachableException, MetricNotFoundException, PluginException {
		double doubleVal = 0d;
		if (metric.toString().startsWith("f10-")) {
			Metric f10Metric = Metric.parse(metric.toString().replace("f10-:", ""));
			MetricValue value = super.getValue(f10Metric);
			doubleVal = value.getValue();
			log.debug("f10 Metric value = " + doubleVal);
			if (doubleVal == 1d) {
				doubleVal = MeasurementConstants.AVAIL_UP;
			} else if (doubleVal == 2d) {
				doubleVal = MeasurementConstants.AVAIL_DOWN;
			} else {
				doubleVal = MeasurementConstants.AVAIL_UNKNOWN;
			}
		}
	return new MetricValue(doubleVal);
	}
}
