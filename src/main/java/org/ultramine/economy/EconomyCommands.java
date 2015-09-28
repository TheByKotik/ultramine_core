package org.ultramine.economy;

import static net.minecraft.util.EnumChatFormatting.*;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;

public class EconomyCommands
{
	@Command(
			name = "pay",
			aliases = {"mpay", "madd"},
			group = "economy",
			permissions = {"command.economy.pay"},
			syntax = {
					"<player> <amount>",
					"<player> <currency> <amount>"
			}
	)
	public static void pay(CommandContext ctx)
	{
		Currency cur = ctx.contains("currency") ? CurrencyRegistry.getCurrency(ctx.get("currency").asString()) : CurrencyRegistry.GSC;
		ctx.check(cur != null, "economy.fail.currency");
		IHoldings from = ctx.getSenderAsPlayer().getData().core().getAccount().getHoldingsOf(cur);
		IHoldings to = ctx.get("player").asPlayerData().core().getAccount().getHoldingsOf(cur);
		double amount = ctx.get("amount").asDouble();
		from.transactChecked(to, amount);
		ctx.sendMessage(DARK_GREEN, GREEN, "command.pay.sended", ctx.get("player").asString(), cur.format(amount));
		ctx.get("player").asOfflinePlayer().sendMessage(DARK_GREEN, GREEN, "command.pay.received", cur.format(amount), ctx.getSenderAsPlayer().func_145748_c_());
	}
	
	@Command(
			name = "money",
			group = "economy",
			permissions = {"command.economy.money", "command.economy.money.other"},
			syntax = {
					"",
					"<player>",
					"<player> <currency>"
			}
	)
	public static void money(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("player", "command.economy.money.other", "command.money.other.fail.perm");
		Currency cur = ctx.contains("currency") ? CurrencyRegistry.getCurrency(ctx.get("currency").asString()) :CurrencyRegistry.GSC;
		ctx.check(cur != null, "economy.fail.currency");
		IHoldings holdings = (ctx.contains("player") ? ctx.get("player").asPlayerData() : ctx.getSenderAsPlayer().getData()).core().getAccount().getHoldingsOf(cur);
		ctx.sendMessage(DARK_GREEN, GREEN, "command.money.info", holdings.getAccount().getName(), cur.format(holdings.getBalance()));
	}
	
	@Command(
			name = "msub",
			aliases = {"msubtract"},
			group = "economy",
			permissions = {"command.economy.msub"},
			syntax = {
					"<player> <amount>",
					"<player> <currency> <amount>"
			}
	)
	public static void msub(CommandContext ctx)
	{
		Currency cur = ctx.contains("currency") ? CurrencyRegistry.getCurrency(ctx.get("currency").asString()) : CurrencyRegistry.GSC;
		ctx.check(cur != null, "economy.fail.currency");
		IHoldings from = ctx.getSenderAsPlayer().getData().core().getAccount().getHoldingsOf(cur);
		IHoldings to = ctx.get("player").asPlayerData().core().getAccount().getHoldingsOf(cur);
		double amount = ctx.get("amount").asDouble();
		ctx.check(amount > 0.0d, "economy.fail.negativeamount");
		to.subtract(amount); //result may be negative
		from.add(amount);
		ctx.sendMessage(DARK_GREEN, GREEN, "command.msub.sended", cur.format(amount), ctx.get("player").asString());
		ctx.get("player").asOfflinePlayer().sendMessage(DARK_GREEN, GREEN, "command.msub.received", cur.format(amount));
	}
	
	@Command(
			name = "mgive",
			group = "economy",
			permissions = {"command.economy.mgive"},
			syntax = {
					"<player> <amount>",
					"<player> <currency> <amount>"
			}
	)
	public static void mgive(CommandContext ctx)
	{
		Currency cur = ctx.contains("currency") ? CurrencyRegistry.getCurrency(ctx.get("currency").asString()) : CurrencyRegistry.GSC;
		ctx.check(cur != null, "economy.fail.currency");
		IHoldings holdings = ctx.get("player").asPlayerData().core().getAccount().getHoldingsOf(cur);
		double amount = ctx.get("amount").asDouble();
		holdings.add(amount);
		ctx.sendMessage(DARK_GREEN, GREEN, "command.mgive.sended", ctx.get("player").asString(), cur.format(amount));
		ctx.get("player").asOfflinePlayer().sendMessage(DARK_GREEN, GREEN, "command.mgive.received", cur.format(amount));
	}
	
	@Command(
			name = "mset",
			group = "economy",
			permissions = {"command.economy.mset"},
			syntax = {
					"<player> <amount>",
					"<player> <currency> <amount>"
			}
	)
	public static void mset(CommandContext ctx)
	{
		Currency cur = ctx.contains("currency") ? CurrencyRegistry.getCurrency(ctx.get("currency").asString()) : CurrencyRegistry.GSC;
		ctx.check(cur != null, "economy.fail.currency");
		IHoldings holdings = ctx.get("player").asPlayerData().core().getAccount().getHoldingsOf(cur);
		double amount = ctx.get("amount").asDouble(); //may be negative
		double last = holdings.getBalance();
		holdings.setBalance(amount);
		ctx.sendMessage(DARK_GREEN, GREEN, "command.mset.sended", ctx.get("player").asString(), cur.format(last), cur.format(amount));
		ctx.get("player").asOfflinePlayer().sendMessage(DARK_GREEN, GREEN, "command.mset.received", cur.format(last), cur.format(amount));
	}
}
