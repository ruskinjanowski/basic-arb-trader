package com.trader.client;

import java.io.File;
import java.io.IOException;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;

import com.trader.logging.LimitsAndRates;
import com.trader.logging.LoggingUtil;
import com.trader.logging.Transaction;
import com.trader.market.data.AccountData;
import com.trader.market.data.MarketData;
import com.trader.market.data.MarketData.MarketPrice;
import com.trader.single.AccWallet;
import com.trader.single.IOrderFilled;
import com.trader.single.LunoBTCManager;
import com.trader.single.OrderTracker;
import com.trader.utility.Utility;
import com.trader.v6.BitstampTrading;

import arbtrader.controller.MarketEvents;
import arbtrader.controller.MarketEvents.ISpreadListener;
import arbtrader.controller.MarketEvents.ReceivePriority;
import arbtrader.credentials.EMarketType;
import arbtrader.credentials.TraderFolders;
import arbtrader.credentials.TraderFolders.ProgramName;
import arbtrader.model.SpreadChanged;
import arbtrader.stats.TradeLimits;
import arbtrader.stats.limits.MeanStandardDeviation;
import arbtrader.stats.limits.MeanStandardDeviation.Formula;

public class ZAR_USD_Converter implements IOrderFilled, ISpreadListener {

	protected static final File TRANSACTION_FILE = new File(TraderFolders.getLogging(ProgramName.BasicArbTrader),
			"transactions.txt");

	AccWallet wallet;
	final LunoBTCManager luno;
	private final MeanStandardDeviation limitGetter;

	double availableUSD;

	public ZAR_USD_Converter() {
		EMarketType market = EMarketType.ZAR_BTC;
		availableUSD = AccountData.getBalance(EMarketType.USD_BTC, Currency.USD, 1);
		wallet = new AccWallet(market);

		limitGetter = new MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC_PERCDIFF, 24 * 60, 1);
		luno = new LunoBTCManager(market, wallet);

		luno.addOrderFilledListener(this);
		MarketEvents.get(market).addSpreadListener(this, ReceivePriority.HIGH);
	}

	@Override
	public synchronized void spreadChanged() {
		try {
			TradeLimits tradeLimits = getTradeLimits();
			double zarusd = MarketData.INSTANCE.getZARrUSD(1).mid();
			SpreadChanged spread = MarketEvents.getSpread(EMarketType.ZAR_BTC);
			MarketPrice mp = MarketData.INSTANCE.getUSDrBTC(1);

			double rateupper = spread.priceAsk / mp.ask;
			double diffupper = (rateupper - zarusd) / zarusd * 100;

			LimitsAndRates lr = new LimitsAndRates(tradeLimits.upper, tradeLimits.lower, diffupper, 0);
			System.out.println(lr);

			System.out.println("rates: " + lr);

			double buyAmount = AccWallet.roundBTC(availableUSD / mp.ask);
			if (diffupper > tradeLimits.upper) {
				// sell BTC
				System.out.println("Trading...");
				if (buyAmount > wallet.getBtc()) {
					luno.setWantedBTC(0);
				} else {
					luno.setWantedBTC(wallet.getBtc() - availableUSD);
				}
			} else {
				// don't trade
				System.out.println("Not trading...");
				luno.setWantedBTC(wallet.getBtc());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	double vol;

	@Override
	public synchronized void orderFilled(OrderTracker t) {
		try {
			vol += Utility.volumeVector(t.getFill(), t.orderType);

			org.knowm.xchange.dto.Order oBitstamp = BitstampTrading.placeOrder(t.getFill());

			//
			Transaction lunoT = Transaction.fromTracker(t, CurrencyPair.BTC_ZAR);
			LoggingUtil.appendToFile(TRANSACTION_FILE, lunoT.toString());
			Transaction bitstampT = new Transaction(oBitstamp.getId(), oBitstamp.getCumulativeAmount().doubleValue(),
					oBitstamp.getAveragePrice().doubleValue(), OrderType.BID, CurrencyPair.BTC_USD);
			LoggingUtil.appendToFile(TRANSACTION_FILE, bitstampT.toString());

			double dollarSpent = oBitstamp.getCumulativeAmount().doubleValue()
					* oBitstamp.getAveragePrice().doubleValue();

			availableUSD -= dollarSpent;
			if (Utility.isEqualVolume(wallet.getBtc(), 0)) {
				System.out.println("volume complete: " + vol);
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	TradeLimits tl;
	long tlTime = 0;

	private TradeLimits getTradeLimits() {
		if (System.currentTimeMillis() - tlTime > 5 * 60 * 1000) {
			try {
				tl = limitGetter.getTradeLimits();
				double dp = tl.upper - tl.lower;
				if (dp < 1) {
					System.out.println("limits too small: " + tl);
					double mid = (tl.upper + tl.lower) / 2;

					tl = new TradeLimits(mid + 0.5, mid - 0.5);
				} else {
					// okay
				}
				tlTime = System.currentTimeMillis();
				System.out.println("updated tradeLimimits: " + tl);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return tl;
	}

}
