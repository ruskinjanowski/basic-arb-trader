package com.trader.bat;

import com.trader.api.Api;
import com.trader.bitstamp.BitstampTrading;
import com.trader.definitions.TraderFolders.ProgramName;

public class BasicArbAux {

	public static void main(String[] args) {

		Api.createApis(ProgramName.BasicArbTrader);

		// System.out.println(AccountData.getBalance(EMarketType.USD_BTC, Currency.USD,
		// 1));
		// System.out.println(AccountData.getBalance(EMarketType.USD_BTC, Currency.BTC,
		// 1));

		// // System.out.println(AccountData.getBitstampBTC(1));
		// System.out.println(AccountData.getBitstampUSD(1));
		/** Send */
		// BitstampTrading.placeOrder(0.33);

		BitstampTrading.sendToLuno(0.33);
	}
}
