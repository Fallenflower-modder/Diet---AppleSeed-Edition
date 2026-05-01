package net.appleseed.appleseed.api.type;

import java.util.Map;

@FunctionalInterface
public interface IDietResult {

    IDietResult EMPTY = Map::of;

    Map<IDietGroup, Float> get();
}
