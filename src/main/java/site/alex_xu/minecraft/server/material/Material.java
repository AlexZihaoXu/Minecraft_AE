package site.alex_xu.minecraft.server.material;

public final class Material {

    public static final Material AIR = new Builder().allowsMovement().lightPassesThrough().notSolid().replaceable().build();
    public static final Material STONE = new Builder().build();
    public static final Material GRASS = new Builder().build();

    private final boolean blocksMovement;
    private final boolean burnable;
    private final boolean liquid;
    private final boolean blocksLight;
    private final boolean replaceable;
    private final boolean solid;

    public Material(boolean liquid, boolean solid, boolean blocksMovement, boolean blocksLight, boolean burnable, boolean replaceable) {
        this.liquid = liquid;
        this.solid = solid;
        this.blocksMovement = blocksMovement;
        this.blocksLight = blocksLight;
        this.burnable = burnable;
        this.replaceable = replaceable;
    }

    public boolean isLiquid() {
        return this.liquid;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean blocksMovement() {
        return this.blocksMovement;
    }

    public boolean isBurnable() {
        return this.burnable;
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean blocksLight() {
        return this.blocksLight;
    }

    public static class Builder {
        private boolean blocksMovement = true;
        private boolean burnable;
        private boolean liquid;
        private boolean replaceable;
        private boolean solid = true;
        private boolean blocksLight = true;

        public Builder() {

        }

        public Builder liquid() {
            this.liquid = true;
            return this;
        }

        public Builder notSolid() {
            this.solid = false;
            return this;
        }

        public Builder allowsMovement() {
            this.blocksMovement = false;
            return this;
        }

        Builder lightPassesThrough() {
            this.blocksLight = false;
            return this;
        }

        protected Builder burnable() {
            this.burnable = true;
            return this;
        }

        public Builder replaceable() {
            this.replaceable = true;
            return this;
        }

        public Material build() {
            return new Material(this.liquid, this.solid, this.blocksMovement, this.blocksLight, this.burnable, this.replaceable);
        }
    }
}
