package io.github.nyliummc.essentials.entities;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

public class BlockStateUtil {
    protected static <T extends Comparable<T>> String getPropertyValue(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }

    protected static String propertiesToString(BlockState state) {
        StringBuilder result = new StringBuilder();
        int count = 0;

        for (Property<?> property : state.getProperties()) {
            if ((count++) > 0) {
                result.append(',');
            }
            result.append(property.getName()).append('=').append(getPropertyValue(state, property));
        }

        return result.toString();
    }
}
