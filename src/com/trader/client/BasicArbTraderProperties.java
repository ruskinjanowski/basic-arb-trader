package com.trader.client;

import java.io.File;

import com.trader.controller.accounts.TradeProperties;

import arbtrader.credentials.TraderFolders;
import arbtrader.credentials.TraderFolders.ProgramName;

public class BasicArbTraderProperties extends TradeProperties {

	public static final BasicArbTraderProperties INSTANCE = new BasicArbTraderProperties();
	public final double profit_perc;
	public final double volume_btc;

	private BasicArbTraderProperties() {
		super(new File(TraderFolders.getConfig(ProgramName.BasicArbTrader), "BasicArbTrader.properties"));
		// TODO Auto-generated constructor stub

		profit_perc = getProperty("profit_perc");
		volume_btc = getProperty("volume_btc");

		System.out.println("-------------Limits---------------");
		System.out.println("profit_perc: " + profit_perc);
		System.out.println("volume_btc : " + volume_btc);
		System.out.println("----------------------------------");

	}

}
