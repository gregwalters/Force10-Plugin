package com.contegix.gregwalters.hyperic.hq.plugin.force10;

import org.hyperic.hq.plugin.netdevice.NetworkDevicePlatformDetector;
import org.hyperic.util.config.ConfigResponse;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.PlatformResource;

public class force10PlatformDetector extends NetworkDevicePlatformDetector {

	public PlatformResource getPlatformResource(ConfigResponse config) throws PluginException {
		System.out.println("Holy shit it worked!!!");
		return super.getPlatformResource(config);
	}
}
