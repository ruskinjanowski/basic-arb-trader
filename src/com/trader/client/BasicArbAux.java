package com.trader.client;

import org.knowm.xchange.currency.Currency;

import com.trader.controller.api.Api;
import com.trader.market.data.AccountData;

import arbtrader.credentials.EMarketType;
import arbtrader.credentials.TraderFolders.ProgramName;

public class BasicArbAux {

	public static void main(String[] args) {
		// /** AccountData */
		Api.createApis(ProgramName.BasicArbTrader);
		System.out.println(AccountData.getBalance(EMarketType.USD_BTC, Currency.USD, 1));
		System.out.println(AccountData.getBalance(EMarketType.USD_BTC, Currency.BTC, 1));
		// System.out.println(AccountData.getBitstampBTC(1));
		// System.out.println(AccountData.getBitstampUSD(1));
		/** Send */
		// BitstampTrading.placeOrder(0.99);

		// BitstampTrading.sendToLuno(1.05);
	}
}
