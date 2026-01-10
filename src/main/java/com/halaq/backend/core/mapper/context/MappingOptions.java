package com.halaq.backend.core.mapper.context;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class MappingOptions {

    public enum Feature {
        INCLUDE_BARBER,
        INCLUDE_CLIENT,
        INCLUDE_SERVICES,
        // tu peux en rajouter ici au fur et à mesure
    }

    private final EnumSet<Feature> features;

    private MappingOptions(EnumSet<Feature> features) {
        this.features = features;
    }

    public static MappingOptions of(Feature... features) {
        return new MappingOptions(features.length == 0
                ? EnumSet.noneOf(Feature.class)
                : EnumSet.copyOf(Arrays.asList(features)));
    }

    public static MappingOptions empty() {
        return new MappingOptions(EnumSet.noneOf(Feature.class));
    }

    public boolean has(Feature feature) {
        return features.contains(feature);
    }

    public Set<Feature> getFeatures() {
        return features;
    }


}
