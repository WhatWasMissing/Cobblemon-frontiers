package com.whatwasmissing.cobblemongacha.network;

import com.whatwasmissing.cobblemongacha.config.GachaConfig;
import com.whatwasmissing.cobblemongacha.core.GachaBanner;
import com.whatwasmissing.cobblemongacha.core.GachaEntry;
import com.whatwasmissing.cobblemongacha.core.GachaRarity;
import com.whatwasmissing.cobblemongacha.core.GachaService;
import com.whatwasmissing.cobblemongacha.core.UpgradeCatalog;
import com.whatwasmissing.cobblemongacha.core.UpgradeTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

/** Server-authoritative presentation data for the custom dashboards. */
public record GachaMenuSyncPayload(
        int menuId,
        boolean drawMenu,
        int bannerIndex,
        int activeBannerIndex,
        long secondsUntilRotation,
        long serverEpochSeconds,
        long gamblingCooldownSeconds,
        int bannerRotationHours,
        int tickets,
        int capturesPerTicket,
        int captureProgress,
        int singleDrawCost,
        int tenDrawCost,
        int rarePityDraws,
        int rarePity,
        int legendaryPityDraws,
        int legendaryPity,
        double shinyChance,
        double legendaryPokemonChance,
        double legendaryPokemonMinimumSourceValue,
        List<BannerData> banners,
        List<TargetData> itemTargets,
        List<TargetData> pokemonTargets) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {

    private static final int MAX_BANNERS = 256;
    private static final int MAX_ENTRIES_PER_BANNER = 512;
    private static final int MAX_TARGETS = 2048;
    private static final int MAX_STRING_LENGTH = 512;

    public static final Type<GachaMenuSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaService.MOD_ID, "menu_sync"));

    public static final StreamCodec<FriendlyByteBuf, GachaMenuSyncPayload> CODEC = new StreamCodec<>() {
        @Override
        public GachaMenuSyncPayload decode(FriendlyByteBuf buffer) {
            int menuId = buffer.readVarInt();
            boolean drawMenu = buffer.readBoolean();
            int bannerIndex = buffer.readVarInt();
            int activeBannerIndex = buffer.readVarInt();
            long secondsUntilRotation = buffer.readVarLong();
            long serverEpochSeconds = buffer.readLong();
            long gamblingCooldownSeconds = buffer.readVarLong();
            int bannerRotationHours = buffer.readVarInt();
            int tickets = buffer.readVarInt();
            int capturesPerTicket = buffer.readVarInt();
            int captureProgress = buffer.readVarInt();
            int singleDrawCost = buffer.readVarInt();
            int tenDrawCost = buffer.readVarInt();
            int rarePityDraws = buffer.readVarInt();
            int rarePity = buffer.readVarInt();
            int legendaryPityDraws = buffer.readVarInt();
            int legendaryPity = buffer.readVarInt();
            double shinyChance = buffer.readDouble();
            double legendaryPokemonChance = buffer.readDouble();
            double legendaryPokemonMinimumSourceValue = buffer.readDouble();

            int bannerCount = boundedCount(buffer.readVarInt(), MAX_BANNERS);
            List<BannerData> banners = new ArrayList<>(bannerCount);
            for (int index = 0; index < bannerCount; index++) {
                String id = buffer.readUtf(MAX_STRING_LENGTH);
                String title = buffer.readUtf(MAX_STRING_LENGTH);
                String description = buffer.readUtf(MAX_STRING_LENGTH);
                int entryCount = boundedCount(buffer.readVarInt(), MAX_ENTRIES_PER_BANNER);
                List<EntryData> entries = new ArrayList<>(entryCount);
                for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
                    entries.add(new EntryData(buffer.readUtf(MAX_STRING_LENGTH),
                            buffer.readUtf(MAX_STRING_LENGTH), buffer.readUtf(MAX_STRING_LENGTH), buffer.readDouble()));
                }
                banners.add(new BannerData(id, title, description, entries));
            }

            List<TargetData> itemTargets = readTargets(buffer);
            List<TargetData> pokemonTargets = readTargets(buffer);
            return new GachaMenuSyncPayload(menuId, drawMenu, bannerIndex, activeBannerIndex,
                    secondsUntilRotation, serverEpochSeconds, gamblingCooldownSeconds, bannerRotationHours, tickets, capturesPerTicket, captureProgress, singleDrawCost,
                    tenDrawCost, rarePityDraws, rarePity, legendaryPityDraws, legendaryPity, shinyChance,
                    legendaryPokemonChance, legendaryPokemonMinimumSourceValue, banners, itemTargets, pokemonTargets);
        }

        @Override
        public void encode(FriendlyByteBuf buffer, GachaMenuSyncPayload payload) {
            buffer.writeVarInt(payload.menuId);
            buffer.writeBoolean(payload.drawMenu);
            buffer.writeVarInt(payload.bannerIndex);
            buffer.writeVarInt(payload.activeBannerIndex);
            buffer.writeVarLong(Math.max(0L, payload.secondsUntilRotation));
            buffer.writeLong(payload.serverEpochSeconds);
            buffer.writeVarLong(Math.max(0L, payload.gamblingCooldownSeconds));
            buffer.writeVarInt(Math.max(1, payload.bannerRotationHours));
            buffer.writeVarInt(Math.max(0, payload.tickets));
            buffer.writeVarInt(Math.max(1, payload.capturesPerTicket));
            buffer.writeVarInt(Math.max(0, payload.captureProgress));
            buffer.writeVarInt(Math.max(1, payload.singleDrawCost));
            buffer.writeVarInt(Math.max(1, payload.tenDrawCost));
            buffer.writeVarInt(Math.max(1, payload.rarePityDraws));
            buffer.writeVarInt(Math.max(0, payload.rarePity));
            buffer.writeVarInt(Math.max(1, payload.legendaryPityDraws));
            buffer.writeVarInt(Math.max(0, payload.legendaryPity));
            buffer.writeDouble(payload.shinyChance);
            buffer.writeDouble(payload.legendaryPokemonChance);
            buffer.writeDouble(payload.legendaryPokemonMinimumSourceValue);

            List<BannerData> banners = payload.banners == null ? List.of() : payload.banners;
            buffer.writeVarInt(Math.min(MAX_BANNERS, banners.size()));
            for (int index = 0; index < banners.size() && index < MAX_BANNERS; index++) {
                BannerData banner = banners.get(index);
                writeString(buffer, banner == null ? "" : banner.id);
                writeString(buffer, banner == null ? "" : banner.title);
                writeString(buffer, banner == null ? "" : banner.description);
                List<EntryData> entries = banner == null || banner.entries == null ? List.of() : banner.entries;
                buffer.writeVarInt(Math.min(MAX_ENTRIES_PER_BANNER, entries.size()));
                for (int entryIndex = 0; entryIndex < entries.size() && entryIndex < MAX_ENTRIES_PER_BANNER; entryIndex++) {
                    EntryData entry = entries.get(entryIndex);
                    writeString(buffer, entry == null ? "" : entry.species);
                    writeString(buffer, entry == null ? "" : entry.displayName);
                    writeString(buffer, entry == null ? "" : entry.rarity);
                    buffer.writeDouble(entry == null ? 0.0 : entry.weight);
                }
            }
            writeTargets(buffer, payload.itemTargets);
            writeTargets(buffer, payload.pokemonTargets);
        }
    };

    public GachaMenuSyncPayload {
        banners = banners == null ? List.of() : List.copyOf(banners);
        itemTargets = itemTargets == null ? List.of() : List.copyOf(itemTargets);
        pokemonTargets = pokemonTargets == null ? List.of() : List.copyOf(pokemonTargets);
    }

    public static GachaMenuSyncPayload forDrawMenu(int menuId, int bannerIndex, UUID playerId) {
        GachaConfig config = GachaService.config();
        return create(menuId, true, bannerIndex, playerId, config.banners,
                config, GachaService.pity(playerId), GachaService.legendaryPity(playerId));
    }

    public static GachaMenuSyncPayload forUpgradeMenu(int menuId, UUID playerId) {
        GachaConfig config = GachaService.config();
        return create(menuId, false, 0, playerId, List.of(), config,
                GachaService.pity(playerId), GachaService.legendaryPity(playerId));
    }

    private static GachaMenuSyncPayload create(int menuId, boolean drawMenu, int bannerIndex, UUID playerId,
                                               List<GachaBanner> banners, GachaConfig config,
                                               int rarePity, int legendaryPity) {
        return new GachaMenuSyncPayload(menuId, drawMenu, bannerIndex, GachaService.activeBannerIndex(),
                GachaService.secondsUntilBannerRotation(), Instant.now().getEpochSecond(),
                GachaService.gamblingCooldownRemainingSeconds(playerId), config.bannerRotationHours,
                GachaService.tickets(playerId),
                config.capturesPerTicket, GachaService.captureProgress(playerId), config.singleDrawCost,
                config.tenDrawCost, config.rarePityDraws, rarePity, config.legendaryPityDraws,
                legendaryPity, config.shinyChance, config.legendaryPokemonChance,
                config.legendaryPokemonMinimumSourceValue, banners.stream().map(BannerData::from).toList(),
                targets(UpgradeCatalog.TargetCategory.ITEMS), targets(UpgradeCatalog.TargetCategory.POKEMON));
    }

    private static List<TargetData> targets(UpgradeCatalog.TargetCategory category) {
        return UpgradeCatalog.targets(category).stream().limit(MAX_TARGETS).map(TargetData::from).toList();
    }

    public List<GachaBanner> toBanners() {
        return banners.stream().map(BannerData::toBanner).toList();
    }

    public List<UpgradeTarget> toItemTargets() {
        return itemTargets.stream().map(TargetData::toTarget).toList();
    }

    public List<UpgradeTarget> toPokemonTargets() {
        return pokemonTargets.stream().map(TargetData::toTarget).toList();
    }

    private static int boundedCount(int count, int maximum) {
        if (count < 0 || count > maximum) throw new IllegalArgumentException("Invalid Cobblemon Gacha sync size");
        return count;
    }

    private static List<TargetData> readTargets(FriendlyByteBuf buffer) {
        int count = boundedCount(buffer.readVarInt(), MAX_TARGETS);
        List<TargetData> targets = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            targets.add(new TargetData(buffer.readUtf(MAX_STRING_LENGTH), buffer.readUtf(MAX_STRING_LENGTH),
                    buffer.readBoolean(), buffer.readUtf(MAX_STRING_LENGTH), buffer.readUtf(MAX_STRING_LENGTH)));
        }
        return targets;
    }

    private static void writeTargets(FriendlyByteBuf buffer, List<TargetData> targets) {
        List<TargetData> safeTargets = targets == null ? List.of() : targets;
        buffer.writeVarInt(Math.min(MAX_TARGETS, safeTargets.size()));
        for (int index = 0; index < safeTargets.size() && index < MAX_TARGETS; index++) {
            TargetData target = safeTargets.get(index);
            writeString(buffer, target == null ? "" : target.id);
            writeString(buffer, target == null ? "" : target.displayName);
            buffer.writeBoolean(target != null && target.pokemon);
            writeString(buffer, target == null ? GachaRarity.COMMON.name() : target.rarity);
            writeString(buffer, target == null ? ChatFormatting.WHITE.name() : target.formatting);
        }
    }

    private static void writeString(FriendlyByteBuf buffer, String value) {
        String safe = value == null ? "" : value;
        int end = safe.length();
        while (end > 0 && (end > MAX_STRING_LENGTH
                || safe.substring(0, end).getBytes(StandardCharsets.UTF_8).length > MAX_STRING_LENGTH)) {
            end = safe.offsetByCodePoints(end, -1);
        }
        buffer.writeUtf(safe.substring(0, end));
    }

    public record BannerData(String id, String title, String description, List<EntryData> entries) {
        private static BannerData from(GachaBanner banner) {
            return new BannerData(banner.id, banner.title, banner.description,
                    banner.entries == null ? List.of() : banner.entries.stream().map(EntryData::from).toList());
        }

        private GachaBanner toBanner() {
            List<GachaEntry> converted = entries == null ? new ArrayList<>()
                    : new ArrayList<>(entries.stream().map(EntryData::toEntry).toList());
            return new GachaBanner(id, title, description, converted);
        }
    }

    public record EntryData(String species, String displayName, String rarity, double weight) {
        private static EntryData from(GachaEntry entry) {
            return new EntryData(entry.species, entry.displayName,
                    entry.rarity == null ? GachaRarity.COMMON.name() : entry.rarity.name(), entry.weight);
        }

        private GachaEntry toEntry() {
            GachaRarity parsed;
            try { parsed = GachaRarity.valueOf(rarity); }
            catch (IllegalArgumentException | NullPointerException ignored) { parsed = GachaRarity.COMMON; }
            return new GachaEntry(species, displayName, parsed, weight);
        }
    }

    public record TargetData(String id, String displayName, boolean pokemon, String rarity, String formatting) {
        private static TargetData from(UpgradeTarget target) {
            return new TargetData(target.id(), target.displayName(), target.pokemon(), target.rarity().name(),
                    target.formatting().name());
        }

        private UpgradeTarget toTarget() {
            GachaRarity parsedRarity;
            try { parsedRarity = GachaRarity.valueOf(rarity); }
            catch (IllegalArgumentException | NullPointerException ignored) { parsedRarity = GachaRarity.COMMON; }
            ChatFormatting parsedFormatting;
            try { parsedFormatting = ChatFormatting.valueOf(formatting); }
            catch (IllegalArgumentException | NullPointerException ignored) { parsedFormatting = ChatFormatting.WHITE; }
            if (pokemon) {
                String species = id == null ? "" : id;
                int separator = species.indexOf(':');
                if (separator >= 0) species = species.substring(separator + 1);
                return UpgradeTarget.pokemon(species, displayName, parsedRarity, parsedFormatting);
            }
            ResourceLocation location = id == null ? null : ResourceLocation.tryParse(id);
            Item item = location == null ? Items.PAPER : BuiltInRegistries.ITEM.get(location);
            if (item == null || item == Items.AIR) item = Items.PAPER;
            return new UpgradeTarget(id, displayName, item, parsedFormatting);
        }
    }

    @Override
    public @NotNull Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
}
