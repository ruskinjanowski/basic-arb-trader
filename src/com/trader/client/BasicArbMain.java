package com.trader.client;

import com.trader.controller.api.Api;

import arbtrader.credentials.TraderFolders.ProgramName;

public class BasicArbMain {
	public static void main(String[] args) {
		Api.createApis(ProgramName.BasicArbTrader);
		ZAR_USD_Converter c = new ZAR_USD_Converter();
		EventClientEndpoint.startClient();

	}

}
