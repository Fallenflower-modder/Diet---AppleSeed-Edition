package net.appleseed.appleseed.common.data.suite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.appleseed.appleseed.api.type.IDietCondition;
import net.appleseed.appleseed.api.type.IDietEffect;
import net.appleseed.appleseed.api.type.IDietSuite;
import net.appleseed.appleseed.common.data.effect.DietAttribute;
import net.appleseed.appleseed.common.data.effect.DietCondition;
import net.appleseed.appleseed.common.data.effect.DietEffect;
import net.appleseed.appleseed.common.data.effect.DietStatusEffect;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

public class DietSuites extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public static final DietSuites SERVER = new DietSuites();
    public static final DietSuites CLIENT = new DietSuites();

    private final Map<String, IDietSuite> suites = new HashMap<>();

    public DietSuites() {
        super(GSON, "diet/suites");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        this.suites.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            JsonObject json = entry.getValue().getAsJsonObject();
            String name = entry.getKey().getPath();
            DietSuite.Builder builder = new DietSuite.Builder(name);

            if (json.has("groups")) {
                for (JsonElement groupElem : json.getAsJsonArray("groups")) {
                    String groupName = groupElem.getAsString();
                    DietGroups.getGroups(null).stream()
                            .filter(g -> g.getName().equals(groupName))
                            .findFirst()
                            .ifPresent(builder::addGroup);
                }
            }

            if (json.has("effects")) {
                for (JsonElement effectElem : json.getAsJsonArray("effects")) {
                    JsonObject effectObj = effectElem.getAsJsonObject();
                    DietEffect.Builder effectBuilder = new DietEffect.Builder();

                    if (effectObj.has("conditions")) {
                        for (JsonElement condElem : effectObj.getAsJsonArray("conditions")) {
                            JsonObject condObj = condElem.getAsJsonObject();
                            String group = condObj.get("group").getAsString();
                            float min = condObj.has("min") ? condObj.get("min").getAsFloat() : 0.0f;
                            float max = condObj.has("max") ? condObj.get("max").getAsFloat() : 1.0f;
                            effectBuilder.addCondition(new DietCondition(group, min, max));
                        }
                    }

                    if (effectObj.has("attributes")) {
                        for (JsonElement attrElem : effectObj.getAsJsonArray("attributes")) {
                            JsonObject attrObj = attrElem.getAsJsonObject();
                            String attribute = attrObj.get("name").getAsString();
                            double amount = attrObj.get("amount").getAsDouble();
                            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(
                                    attrObj.has("operation") ? attrObj.get("operation").getAsString() : "ADD_VALUE");
                            effectBuilder.addAttribute(new DietAttribute(attribute, amount, operation));
                        }
                    }

                    if (effectObj.has("effects")) {
                        for (JsonElement statusElem : effectObj.getAsJsonArray("effects")) {
                            JsonObject statusObj = statusElem.getAsJsonObject();
                            String effect = statusObj.get("name").getAsString();
                            int amplifier = statusObj.has("amplifier") ? statusObj.get("amplifier").getAsInt() : 0;
                            effectBuilder.addStatusEffect(new DietStatusEffect(effect, amplifier));
                        }
                    }

                    builder.addEffect(effectBuilder.build());
                }
            }

            this.suites.put(name, builder.build());
        }
    }

    public static Collection<IDietSuite> getSuites() {
        return SERVER.suites.values();
    }

    public static IDietSuite getSuite(String name) {
        return SERVER.suites.get(name);
    }
}
