package com.contegix.gregwalters.hyperic.hq.plugin.force10;

import org.hyperic.hq.plugin.netdevice.NetworkDevicePlatformDetector;
import org.hyperic.util.config.ConfigResponse;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.PlatformResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class force10PlatformDetector extends NetworkDevicePlatformDetector {

	private transient Log log =  LogFactory.getLog("force10PlatformDetector");

	public PlatformResource getPlatformResource(ConfigResponse config) throws PluginException {
		log.debug("Starting getPlatformResource(ConfigResponse config)");
		PlatformResource platform = super.getPlatformResource(config);
		log.debug("Got platform");
		//force10ServerDetector detector = new force10ServerDetector();
		//log.debug("Got detector");
		//detector.discoverResources(platform, config);
		//log.debug("Finished running detection.");
		//return detector.discoverResources(platform, config);
		log.debug("Platform object: " + platform.toString());
		return platform;
	}
}
