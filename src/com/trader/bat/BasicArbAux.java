package com.trader.bat;

import com.trader.bitstamp.BitstampTrading;

public class BasicArbAux {

	public static void main(String[] args) {
		// /** AccountData */
		// Api.createApis(ProgramName.BasicArbTrader);
		// System.out.println(AccountData.getBalance(EMarketType.USD_BTC, Currency.USD,
		// 1));
		// System.out.println(AccountData.getBalance(EMarketType.USD_BTC, Currency.BTC,
		// 1));
		// // System.out.println(AccountData.getBitstampBTC(1));
		// System.out.println(AccountData.getBitstampUSD(1));
		/** Send */
		// BitstampTrading.placeOrder(0.99);

		BitstampTrading.sendToLuno(1.05);
	}
}
