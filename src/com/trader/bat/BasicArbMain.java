package com.trader.bat;

import com.trader.client.EventClientEndpoint;
import com.trader.controller.api.Api;
import com.trader.definitions.TraderFolders.ProgramName;

public class BasicArbMain {
	public static void main(String[] args) {
		Api.createApis(ProgramName.BasicArbTrader);
		ZAR_USD_Converter c = new ZAR_USD_Converter();
		EventClientEndpoint.startClient();

	}

}
