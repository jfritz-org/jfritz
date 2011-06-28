package de.moonflower.jfritz.proxy;

import java.net.Proxy;

import de.moonflower.jfritz.properties.PropertyProvider;
import de.robotniko.proxy.api.ProxyFacade;

public class JFritzProxy {

	private static JFritzProxy INSTANCE = new JFritzProxy();

	public static JFritzProxy getInstance() {
		return INSTANCE;
	}

	public Proxy getProxy() {
		boolean proxyActive = Boolean.parseBoolean(PropertyProvider.getInstance().getProperty("option.proxy.active"));
		boolean authRequired  = Boolean.parseBoolean(PropertyProvider.getInstance().getProperty("option.proxy.authRequired"));
		String proxyHost = PropertyProvider.getInstance().getProperty("option.proxy.host");
		String proxyPortString = PropertyProvider.getInstance().getProperty("option.proxy.port");
		int proxyPort = 0;
		if (proxyPortString != null) {
			Integer.parseInt(proxyPortString);
		}
		String proxyUser = PropertyProvider.getInstance().getProperty("option.proxy.user");
		String proxyPassword = PropertyProvider.getInstance().getProperty("option.proxy.password");
		if (authRequired) {
			return ProxyFacade.getProxyService().getProxy(proxyActive, proxyHost, proxyPort, proxyUser, proxyPassword);
		} else {
			return ProxyFacade.getProxyService().getProxy(proxyActive, proxyHost, proxyPort);
		}
	}

}
