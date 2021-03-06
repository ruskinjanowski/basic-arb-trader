package com.trader.bat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;

import com.trader.bitstamp.BitstampTrading;
import com.trader.client.MarketEvents;
import com.trader.client.MarketEvents.ISpreadListener;
import com.trader.client.MarketEvents.ReceivePriority;
import com.trader.definitions.TraderFolders;
import com.trader.definitions.TraderFolders.ProgramName;
import com.trader.logging.LoggingUtil;
import com.trader.logging.Transaction;
import com.trader.luno.AccWallet;
import com.trader.luno.IOrderFilled;
import com.trader.luno.LunoBTCManager;
import com.trader.luno.OrderTracker;
import com.trader.model.MarketType;
import com.trader.model.Spread;
import com.trader.utility.AccountData;
import com.trader.utility.MarketData;
import com.trader.utility.MarketData.MarketPrice;
import com.trader.utility.Utility;

import arbtrader.stats.TradeLimits;
import arbtrader.stats.limits.MeanStandardDeviation;
import arbtrader.stats.limits.MeanStandardDeviation.Formula;

public class ZAR_USD_Converter implements IOrderFilled, ISpreadListener {

	protected static final File TRANSACTION_FILE = new File(TraderFolders.getLogging(ProgramName.BasicArbTrader),
			"transactions.txt");

	AccWallet wallet;
	final LunoBTCManager luno;
	private final MeanStandardDeviation limitGetter;

	final double startingUSD;
	final double startingBTCLuno;
	double tradedBTC = 0;

	// final double toTrade;

	public ZAR_USD_Converter() {
		MarketType market = MarketType.ZAR_BTC;
		startingUSD = AccountData.getBalance(MarketType.USD_BTC, Currency.USD, 1);
		startingBTCLuno = AccountData.getBalance(market, Currency.BTC, 1);
		wallet = new AccWallet(market);

		limitGetter = new MeanStandardDeviation(Formula.USDBITSTAMP_ZARLUNO_BTC_PERCDIFF, 24 * 60,
				BasicArbTraderProperties.INSTANCE.sdMultiple);
		luno = new LunoBTCManager(market, wallet);

		luno.addOrderFilledListener(this);
		MarketEvents.get(market).addSpreadListener(this, ReceivePriority.HIGH);

	}

	@Override
	public synchronized void spreadChanged() {
		try {

			MarketPrice mp = MarketData.INSTANCE.getUSDrBTC(1);

			if (isConditionCorrect()) {

				final double tryy;
				if (BasicArbTraderProperties.INSTANCE.useFixedBTCAmount) {
					tryy = BasicArbTraderProperties.INSTANCE.fixedBTCAmount;
				} else {
					tryy = startingBTCLuno;
				}

				double canBuyBitstamp = startingUSD / mp.ask;
				if (canBuyBitstamp > tryy) {
					if (BasicArbTraderProperties.INSTANCE.useFixedBTCAmount) {
						luno.setWantedBTC(startingBTCLuno - BasicArbTraderProperties.INSTANCE.fixedBTCAmount);
					} else {
						luno.setWantedBTC(0);
					}
				} else {
					luno.setWantedBTC(startingBTCLuno - canBuyBitstamp);
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

			tradedBTC += t.getFill();
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

	/**
	 * Check whether all conditions specified in the properties file are correct,
	 * also logs details.
	 * 
	 * @return whether conditions specified in the properties file are true.
	 */
	private boolean isConditionCorrect() {

		double zarusd = MarketData.INSTANCE.getZARrUSD(1).mid();
		Spread spread = MarketEvents.getSpread(MarketType.ZAR_BTC);
		MarketPrice mp = MarketData.INSTANCE.getUSDrBTC(1);

		double rateupper = spread.priceAsk / mp.ask;
		double diffupper = (rateupper - zarusd) / zarusd * 100;

		AtomicBoolean total = new AtomicBoolean(true);
		String log = "";
		if (BasicArbTraderProperties.INSTANCE.useMeanSd) {
			TradeLimits tradeLimits = getTradeLimits();
			log = append("MeanSd", log, total, diffupper, tradeLimits.upper);
		}
		if (BasicArbTraderProperties.INSTANCE.useDiff) {
			log = append("Diff", log, total, diffupper, BasicArbTraderProperties.INSTANCE.diff);
		}
		if (BasicArbTraderProperties.INSTANCE.useExRate) {
			log = append("Rate", log, total, rateupper, BasicArbTraderProperties.INSTANCE.exRate);
		}

		System.out.println(log);
		return total.get();
	}

	private String append(String description, String existing, AtomicBoolean total, double actual, double limit) {
		boolean eval = actual > limit;
		total.set(total.get() & eval);
		// logging
		actual = Math.floor(actual * 10_000) / 10_000;
		limit = Math.floor(limit * 10_000) / 10_000;
		String toAppend = description + " " + eval + " " + actual + ">" + limit + "  ";

		return existing + toAppend;
	}

}
