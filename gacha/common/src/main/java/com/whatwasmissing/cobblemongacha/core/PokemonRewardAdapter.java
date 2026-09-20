package com.whatwasmissing.cobblemongacha.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * Isolates the version-sensitive Cobblemon species/storage calls. The rest of
 * the gacha system only deals with a species id and a result object. Reflection
 * lets the source remain usable across the small party API changes between
 * Cobblemon 1.8.x builds; failure is converted into a visible voucher instead
 * of silently deleting a paid pull.
 */
public final class PokemonRewardAdapter {
    private static final String SPECIES_REGISTRY = "com.cobblemon.mod.common.api.pokemon.PokemonSpecies";
    private static final String COBBLEMON = "com.cobblemon.mod.common.Cobblemon";

    private PokemonRewardAdapter() {}

    public static boolean deliver(ServerPlayer player, GachaResult result) {
        try {
            Object species = findSpecies(result.species());
            if (species == null) return false;

            Object pokemon = createPokemon(species);
            if (pokemon == null) return false;
            if (result.shiny() && !setShiny(pokemon, true)) return false;

            Object storage = storageObject();
            if (storage == null) return false;
            Object party = getStore(storage, "getParty", player);
            if (party != null) {
                try {
                    if (addPokemon(party, pokemon)) return true;
                } catch (ReflectiveOperationException | RuntimeException exception) {
                    GachaService.LOGGER.debug("Party delivery was unavailable for {}. Trying PC.", result.species(), exception);
                }
            }

            Object pc = getStore(storage, "getPC", player);
            if (pc != null) {
                try {
                    return addPokemon(pc, pokemon);
                } catch (ReflectiveOperationException | RuntimeException exception) {
                    GachaService.LOGGER.debug("PC delivery was unavailable for {}.", result.species(), exception);
                }
            }
            return false;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            GachaService.LOGGER.warn("Could not deliver gacha result {} to {}. Giving a voucher instead.",
                    result.species(), player.getGameProfile().getName(), exception);
            return false;
        }
    }

    public static void giveVoucher(ServerPlayer player, GachaResult result) {
        ItemStack voucher = createVoucher(result);
        if (!player.getInventory().add(voucher)) player.drop(voucher, false);
    }

    /** Redeems the identity-preserving voucher in the player's main hand. */
    public static boolean redeemVoucher(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        GachaResult result = readVoucher(held);
        if (result == null) {
            player.sendSystemMessage(Component.literal("Hold a Cobblemon Gacha voucher in your main hand.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (!deliver(player, result)) {
            player.sendSystemMessage(Component.literal("There is no room in your party or PC for this voucher.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        held.shrink(1);
        player.sendSystemMessage(Component.literal("Redeemed " + result.label() + ".")
                .withStyle(result.rarity().formatting()));
        return true;
    }

    private static ItemStack createVoucher(GachaResult result) {
        ItemStack voucher = new ItemStack(Items.PAPER);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("cobblemon_gacha_voucher", true);
        tag.putString("species", result.species() == null ? "" : result.species());
        tag.putString("display_name", result.displayName() == null ? result.species() : result.displayName());
        tag.putString("rarity", result.rarity().name());
        tag.putBoolean("shiny", result.shiny());
        voucher.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        voucher.set(DataComponents.CUSTOM_NAME, Component.literal("Pokémon Contract: " + result.label())
                .withStyle(result.rarity().formatting()));
        return voucher;
    }

    private static GachaResult readVoucher(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag tag = customData.copyTag();
        if (!tag.getBoolean("cobblemon_gacha_voucher")) return null;
        String species = tag.getString("species");
        String displayName = tag.getString("display_name");
        GachaRarity rarity;
        try {
            rarity = GachaRarity.valueOf(tag.getString("rarity"));
        } catch (IllegalArgumentException exception) {
            return null;
        }
        if (species.isBlank() || displayName.isBlank()) return null;
        return new GachaResult(species, displayName, rarity, tag.getBoolean("shiny"));
    }

    private static Object findSpecies(String speciesId) throws ReflectiveOperationException {
        Class<?> registry = Class.forName(SPECIES_REGISTRY);
        Object registryInstance = singletonInstance(registry);
        String identifier = normaliseIdentifier(speciesId);
        Object resourceLocation = resourceLocation(identifier);

        // PokemonSpecies is a Kotlin object in Cobblemon, so its registry
        // methods are instance methods from Java. The old adapter only looked
        // for static getByName methods, which made every valid reward fall back
        // to a voucher even when party/PC storage was available.
        Object value = invokeRegistry(registry, registryInstance, "getByIdentifier", new Object[]{resourceLocation});
        if (value != null) return value;

        String name = identifier.substring(identifier.indexOf(':') + 1);
        value = invokeRegistry(registry, registryInstance, "getByName", new Object[]{name});
        if (value != null) return value;

        // Keep compatibility with older Cobblemon builds that exposed a
        // namespaced getByName overload instead of getByIdentifier.
        String namespace = identifier.substring(0, identifier.indexOf(':'));
        value = invokeRegistry(registry, registryInstance, "getByName", new Object[]{name, namespace});
        if (value != null) return value;
        return null;
    }

    private static Object createPokemon(Object species) throws ReflectiveOperationException {
        for (Method method : species.getClass().getMethods()) {
            if (!method.getName().equals("create")) continue;
            if (method.getParameterCount() == 0) return method.invoke(species);
            // Kotlin's default argument is not emitted as a Java no-arg
            // overload unless @JvmOverloads is present. Cobblemon 1.8.x
            // exposes create(int), whose normal default is level 10.
            if (method.getParameterCount() == 1
                    && (method.getParameterTypes()[0] == int.class
                    || method.getParameterTypes()[0] == Integer.class)) {
                return method.invoke(species, 10);
            }
        }
        return null;
    }

    private static Object singletonInstance(Class<?> type) throws ReflectiveOperationException {
        try {
            Field field = type.getField("INSTANCE");
            return field.get(null);
        } catch (NoSuchFieldException exception) {
            // A Java-facing build may expose registry methods statically.
            return null;
        }
    }

    private static String normaliseIdentifier(String speciesId) {
        String value = speciesId == null ? "" : speciesId.trim().toLowerCase(Locale.ROOT);
        return value.contains(":") ? value : "cobblemon:" + value;
    }

    private static Object resourceLocation(String identifier) throws ReflectiveOperationException {
        Class<?> resourceLocation = Class.forName("net.minecraft.resources.ResourceLocation");
        Method parse = resourceLocation.getMethod("parse", String.class);
        return parse.invoke(null, identifier);
    }

    private static Object invokeRegistry(Class<?> registry, Object instance, String name, Object[] arguments)
            throws ReflectiveOperationException {
        if (instance != null) return invokeCompatible(instance, name, arguments);
        for (Method method : registry.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals(name)
                    || method.getParameterCount() != arguments.length) continue;
            Class<?>[] parameters = method.getParameterTypes();
            boolean compatible = true;
            for (int index = 0; index < parameters.length; index++) {
                if (arguments[index] == null || !parameters[index].isAssignableFrom(arguments[index].getClass())) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) return method.invoke(null, arguments);
        }
        return null;
    }

    private static boolean setShiny(Object pokemon, boolean shiny) throws ReflectiveOperationException {
        for (Method method : pokemon.getClass().getMethods()) {
            if (method.getName().equals("setShiny") && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == boolean.class) {
                method.invoke(pokemon, shiny);
                return true;
            }
        }
        for (Field field : pokemon.getClass().getFields()) {
            if (field.getName().equalsIgnoreCase("shiny") && field.getType() == boolean.class) {
                field.set(pokemon, shiny);
                return true;
            }
        }
        return false;
    }

    private static Object storageObject() throws ReflectiveOperationException {
        Class<?> cobblemon = Class.forName(COBBLEMON);
        Object instance = null;
        try {
            Field field = cobblemon.getField("INSTANCE");
            instance = field.get(null);
        } catch (NoSuchFieldException ignored) {
            // Java-facing builds may expose the singleton properties statically.
        }
        Object storage = invokeNoArg(cobblemon, instance, "getStorage");
        return storage != null ? storage : invokeNoArg(cobblemon, instance, "storage");
    }

    private static Object getStore(Object storage, String methodName, ServerPlayer player)
            throws ReflectiveOperationException {
        Object store = invokeCompatible(storage, methodName, new Object[]{player});
        if (store != null) return store;
        store = invokeCompatible(storage, methodName, new Object[]{player.getUUID()});
        if (store != null) return store;
        Object registryAccess = player.server.registryAccess();
        store = invokeCompatible(storage, methodName, new Object[]{player, registryAccess});
        if (store != null) return store;
        return invokeCompatible(storage, methodName, new Object[]{player.getUUID(), registryAccess});
    }

    private static boolean addPokemon(Object store, Object pokemon) throws ReflectiveOperationException {
        for (String name : new String[]{"add", "addPokemon", "addToParty"}) {
            for (Method method : store.getClass().getMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != 1
                        || !method.getParameterTypes()[0].isAssignableFrom(pokemon.getClass())) continue;
                Object returned = method.invoke(store, pokemon);
                if (returned instanceof Boolean booleanResult && !booleanResult) continue;
                return true;
            }
        }
        return false;
    }

    private static Object invokeNoArg(Class<?> type, Object instance, String name)
            throws ReflectiveOperationException {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != 0) continue;
            if (Modifier.isStatic(method.getModifiers())) return method.invoke(null);
            if (instance != null) return method.invoke(instance);
        }
        return null;
    }

    private static Object invokeCompatible(Object target, String name, Object[] arguments)
            throws ReflectiveOperationException {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) continue;
            Class<?>[] parameters = method.getParameterTypes();
            boolean compatible = true;
            for (int index = 0; index < parameters.length; index++) {
                if (arguments[index] == null || !parameters[index].isAssignableFrom(arguments[index].getClass())) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) return method.invoke(target, arguments);
        }
        return null;
    }
}
