package su.nightexpress.coinsengine.currency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.coinsengine.CoinsEngine;
import su.nightexpress.coinsengine.api.currency.Currency;
import su.nightexpress.coinsengine.command.currency.CurrencyMainCommand;
import su.nightexpress.coinsengine.command.currency.impl.BalanceCommand;
import su.nightexpress.coinsengine.command.currency.impl.SendCommand;
import su.nightexpress.coinsengine.command.currency.impl.TopCommand;
import su.nightexpress.coinsengine.config.Config;
import su.nightexpress.coinsengine.currency.impl.ConfigCurrency;
import su.nightexpress.coinsengine.currency.listener.CurrencyListener;
import su.nightexpress.coinsengine.currency.task.BalanceUpdateTask;
import su.nightexpress.coinsengine.hook.VaultEconomyHook;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyManager extends AbstractManager<CoinsEngine> {

    private final Map<String, Currency>                     currencyMap;
    private final Map<Currency, List<Pair<String, Double>>> balanceMap;

    private BalanceUpdateTask balanceUpdateTask;

    public CurrencyManager(@NotNull CoinsEngine plugin) {
        super(plugin);
        this.currencyMap = new HashMap<>();
        this.balanceMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.plugin.getConfigManager().extractResources(Config.DIR_CURRENCIES);

        for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + Config.DIR_CURRENCIES)) {
            ConfigCurrency currency = new ConfigCurrency(plugin, cfg);
            if (currency.load()) {
                this.registerCurrency(currency);
            }
        }

        this.getVaultCurrency().ifPresent(currency -> {
            if (EngineUtils.hasVault()) {
                VaultEconomyHook.setup(this.plugin, currency);
                if (Config.ECONOMY_COMMAND_SHORTCUTS_ENABLED.get()) {
                    this.plugin.getCommandManager().registerCommand(new BalanceCommand(plugin, currency));
                    this.plugin.getCommandManager().registerCommand(new SendCommand(plugin, currency));
                    this.plugin.getCommandManager().registerCommand(new TopCommand(plugin, currency, "baltop"));
                }
            }
            else {
                this.plugin.error("Found Vault Economy currency, but Vault is not installed!");
            }
        });

        this.addListener(new CurrencyListener(this));

        this.balanceUpdateTask = new BalanceUpdateTask(this.plugin);
        this.balanceUpdateTask.start();
    }

    @Override
    protected void onShutdown() {
        if (this.balanceUpdateTask != null) this.balanceUpdateTask.stop();
        if (EngineUtils.hasVault()) {
            VaultEconomyHook.shutdown();
        }
        this.getCurrencyMap().clear();
    }

    @NotNull
    public Map<Currency, List<Pair<String, Double>>> getBalanceMap() {
        return balanceMap;
    }

    @NotNull
    public List<Pair<String, Double>> getBalanceList(@NotNull Currency currency) {
        return this.getBalanceMap().getOrDefault(currency, Collections.emptyList());
    }

    public void registerCurrency(@NotNull Currency currency) {
        this.plugin.getCommandManager().registerCommand(new CurrencyMainCommand(plugin, currency));
        this.getCurrencyMap().put(currency.getId(), currency);
        this.plugin.info("Currency registered: '" + currency.getId() + "'!");
    }

    public void unregisterCurrency(@NotNull Currency currency) {
        Currency del = this.getCurrencyMap().remove(currency.getId());
        if (del == null) return;

        this.plugin.getCommandManager().unregisterCommand(currency.getCommandAliases()[0]);
        this.plugin.info("Currency unregistered: '" + del.getId() + "'!");
    }

    @Nullable
    public Currency getCurrency(@NotNull String id) {
        return this.getCurrencyMap().get(id.toLowerCase());
    }

    @NotNull
    public Map<String, Currency> getCurrencyMap() {
        return currencyMap;
    }

    @NotNull
    public Optional<Currency> getVaultCurrency() {
        return this.getCurrencies().stream().filter(Currency::isVaultEconomy).findFirst();
    }

    @NotNull
    public Collection<Currency> getCurrencies() {
        return this.getCurrencyMap().values();
    }
}
