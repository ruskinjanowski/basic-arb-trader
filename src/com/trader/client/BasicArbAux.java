package com.trader.client;

import com.trader.market.data.AccountData;

public class BasicArbAux {

	public static void main(String[] args) {
		// /** AccountData */
		System.out.println(AccountData.getLunoBTC(1));
		System.out.println(AccountData.getLunoZAR(1));
		// System.out.println(AccountData.getBitstampBTC(1));
		// System.out.println(AccountData.getBitstampUSD(1));
		/** Send */
		// BitstampTrading.placeOrder(0.99);

		// BitstampTrading.sendToLuno(1.05);
	}
}
