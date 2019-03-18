package com.trader.client;

import java.io.File;

import com.trader.controller.accounts.TradeProperties;

import arbtrader.credentials.TraderFolders;
import arbtrader.credentials.TraderFolders.ProgramName;

public class BasicArbTraderProperties extends TradeProperties {

	public static final BasicArbTraderProperties INSTANCE = new BasicArbTraderProperties();

	public final boolean useMeanSd;
	/**
	 * Standard deviation multiple to trade above.
	 */
	public final double sdMultiple;

	public final boolean useDiff;
	/**
	 * Difference to trade above.
	 */
	public final double diff;

	public final boolean useExRate;
	/**
	 * ZAR/USD exchange rate to trade above.
	 */
	public final double exRate;

	public final boolean useFixedBTCAmount;
	/**
	 * Fixed amount to trade.
	 */
	public final double fixedBTCAmount;

	private BasicArbTraderProperties() {
		super(new File(TraderFolders.getConfig(ProgramName.BasicArbTrader), "BasicArbTrader.properties"));

		useMeanSd = getPropertyB("useSdMultiple");
		sdMultiple = getProperty("sdMultiple");
		useDiff = getPropertyB("useDifference");
		diff = getProperty("difference");
		useExRate = getPropertyB("useExchangeRate");
		exRate = getProperty("exchangeRate");
		useFixedBTCAmount = getPropertyB("useFixedBTCAmount");
		fixedBTCAmount = getProperty("fixedBTCAmount");
	}

}
