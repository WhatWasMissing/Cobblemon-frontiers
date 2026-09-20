package com.whatwasmissing.cobblemongacha.gui;

import com.cobblemon.mod.common.api.gui.GuiUtilsKt;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.text.Normalizer;
import java.util.Set;
import java.util.Locale;

/**
 * Draws Cobblemon species through Cobblemon's own profile renderer. This is
 * important because the files under textures/pokemon are model atlases, not
 * standalone UI icons.
 */
public final class PokemonSpriteRenderer {
    private PokemonSpriteRenderer() {}

    public static boolean render(GuiGraphics graphics, String speciesOrLabel, boolean shiny,
                                 int x, int y, int size) {
        Species species = findSpecies(speciesOrLabel);
        if (species == null || species.getResourceIdentifier() == null) return false;

        FloatingState state = new FloatingState();
        state.setCurrentAspects(shiny || isShinyLabel(speciesOrLabel) ? Set.of("shiny") : Set.of());
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        // Cobblemon's profile helper renders a 2x2 unit quad at its default
        // scale of 20, so 40 model units map to one requested UI icon.
        float scale = size / 40.0f;
        graphics.pose().scale(scale, scale, 1.0f);
        try {
            GuiUtilsKt.drawProfile(species.getResourceIdentifier(), graphics.pose(), state, 0.0f, 20.0f);
            return true;
        } catch (RuntimeException ignored) {
            // A third-party species can be registered before its client model
            // is ready. Let the caller use its normal item fallback instead of
            // taking down the whole GUI.
            return false;
        } finally {
            graphics.pose().popPose();
        }
    }

    private static Species findSpecies(String speciesOrLabel) {
        if (speciesOrLabel == null || speciesOrLabel.isBlank()) return null;
        String value = speciesOrLabel.trim();
        if (isShinyLabel(value)) value = value.substring(6).trim();

        int separator = value.indexOf(':');
        if (separator > 0) {
            ResourceLocation identifier = ResourceLocation.tryParse(value.toLowerCase(Locale.ROOT));
            return identifier == null ? null : PokemonSpecies.getByIdentifier(identifier);
        }
        return PokemonSpecies.getByName(normalise(value));
    }

    private static String normalise(String value) {
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return decomposed.toLowerCase(Locale.ROOT).trim().replace(' ', '_');
    }

    private static boolean isShinyLabel(String value) {
        return value != null && value.regionMatches(true, 0, "Shiny ", 0, 6);
    }
}
