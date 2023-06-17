package su.nightexpress.coinsengine.currency.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractConfigHolder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.coinsengine.CoinsEngine;
import su.nightexpress.coinsengine.Placeholders;
import su.nightexpress.coinsengine.api.currency.Currency;

public class ConfigCurrency extends AbstractConfigHolder<CoinsEngine> implements Currency {

    private String name;
    private String symbol;
    private String format;
    private String[] commandAliases;
    private boolean decimal;
    private boolean permissionRequired;
    private boolean transferAllowed;
    private double startValue;
    private double maxValue;
    private boolean vaultEconomy;

    private final PlaceholderMap placeholderMap;

    public ConfigCurrency(@NotNull CoinsEngine plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_ID, this::getId)
            .add(Placeholders.CURRENCY_NAME, this::getName)
            .add(Placeholders.CURRENCY_SYMBOL, this::getSymbol)
        // TODO
        ;
    }

    @Override
    public boolean load() {
        this.name = Colorizer.apply(cfg.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.symbol = Colorizer.apply(cfg.getString("Symbol", this.getName()));
        this.format = Colorizer.apply(cfg.getString("Format", Placeholders.GENERIC_AMOUNT + Placeholders.CURRENCY_SYMBOL));
        this.commandAliases = cfg.getString("Command_Aliases", this.getName()).toLowerCase().split(",");
        this.decimal = cfg.getBoolean("Decimal");
        this.permissionRequired = cfg.getBoolean("Permission_Required");
        this.transferAllowed = cfg.getBoolean("Transfer_Allowed");
        this.startValue = cfg.getDouble("Start_Value", 0D);
        this.maxValue = cfg.getDouble("Max_Value", 0D);
        this.vaultEconomy = cfg.getBoolean("Economy.Vault");
        return true;
    }

    @Override
    public void onSave() {

    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getSymbol() {
        return symbol;
    }

    @NotNull
    @Override
    public String getFormat() {
        return format;
    }

    @NotNull
    @Override
    public String[] getCommandAliases() {
        return commandAliases;
    }

    @Override
    public boolean isDecimal() {
        return decimal;
    }

    @Override
    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    @Override
    public boolean isTransferAllowed() {
        return transferAllowed;
    }

    @Override
    public double getStartValue() {
        return startValue;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean isVaultEconomy() {
        return vaultEconomy;
    }
}