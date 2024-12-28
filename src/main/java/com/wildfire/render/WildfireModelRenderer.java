package com.wildfire.render;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public class WildfireModelRenderer {

    public static class ModelBox {
        public final WildfireModelRenderer.TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            this(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, 5);
        }

        protected ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, int quads) {
            this(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, quads, false);
        }

        protected ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, int quads, boolean extra) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float) dx;
            this.posY2 = y + (float) dy;
            this.posZ2 = z + (float) dz;
            this.quads = new TexturedQuad[quads];
            float f = x + (float) dx;
            float f1 = y + (float) dy;
            float f2 = z + (float) dz;
            x = x - delta;
            y = y - delta;
            z = z - delta;
            f = f + delta;
            f1 = f1 + delta;
            f2 = f2 + delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }
            initQuads(tW, tH, texU, texV, dx, dy, dz, mirror, extra,
                    new PositionTextureVertex(f, y, z, 0.0F, 8.0F),
                    new PositionTextureVertex(f, f1, z, 8.0F, 8.0F),
                    new PositionTextureVertex(x, f1, z, 8.0F, 0.0F),
                    new PositionTextureVertex(x, y, f2, 0.0F, 0.0F),
                    new PositionTextureVertex(f, y, f2, 0.0F, 8.0F),
                    new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F),
                    new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F),
                    new PositionTextureVertex(x, y, z, 0.0F, 0.0F)
            );
        }

        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PositionTextureVertex vertex,
                                 PositionTextureVertex vertex1, PositionTextureVertex vertex2, PositionTextureVertex vertex3, PositionTextureVertex vertex4, PositionTextureVertex vertex5,
                                 PositionTextureVertex vertex6, PositionTextureVertex vertex7) {
            this.quads[0] = new TexturedQuad(texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, tW, tH, mirror, EnumFacing.EAST,
                    vertex4, vertex, vertex1, vertex5);
            this.quads[1] = new TexturedQuad(texU, texV + dz, texU + dz, texV + dz + dy, tW, tH, mirror, EnumFacing.WEST,
                    vertex7, vertex3, vertex6, vertex2);
            this.quads[2] = new TexturedQuad(texU + dz, texV, texU + dz + dx, texV + dz, tW, tH, mirror, EnumFacing.DOWN,
                    vertex4, vertex3, vertex7, vertex);
            this.quads[3] = new TexturedQuad(texU + dz, texV + dz + 4, texU + dz + dx, texV + 1 + dz + dy, tW, tH - 1, mirror, EnumFacing.UP,
                    vertex1, vertex2, vertex6, vertex5);
            this.quads[4] = new TexturedQuad(texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, tW, tH, mirror, EnumFacing.NORTH,
                    vertex, vertex7, vertex2, vertex1);
        }
    }

    public static class OverlayModelBox extends ModelBox {

        public OverlayModelBox(boolean isLeft, int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            super(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, 4, isLeft);
        }

        @Override
        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean isLeft, PositionTextureVertex vertex,
                                 PositionTextureVertex vertex1, PositionTextureVertex vertex2, PositionTextureVertex vertex3, PositionTextureVertex vertex4, PositionTextureVertex vertex5,
                                 PositionTextureVertex vertex6, PositionTextureVertex vertex7) {
            if (!isLeft) {
                this.quads[0] = new TexturedQuad(texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, tW, tH, mirror, EnumFacing.EAST,
                        vertex4, vertex, vertex1, vertex5);
            } else {
                this.quads[0] = new TexturedQuad(texU, texV + dz, texU + dz, texV + dz + dy, tW, tH, mirror, EnumFacing.WEST,
                        vertex7, vertex3, vertex6, vertex2);
            }
            this.quads[1] = new TexturedQuad(texU + dz, texV, texU + dz + dx, texV + dz, tW, tH, mirror, EnumFacing.DOWN,
                    vertex4, vertex3, vertex7, vertex);
            this.quads[2] = new TexturedQuad(texU + dz, texV + dz + 4, texU + dz + dx, texV + 1 + dz + dy, tW, tH - 1, mirror, EnumFacing.UP,
                    vertex1, vertex2, vertex6, vertex5);
            this.quads[3] = new TexturedQuad(texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, tW, tH, mirror, EnumFacing.NORTH,
                    vertex, vertex7, vertex2, vertex1);
        }
    }

    public static class BreastModelBox extends ModelBox {

        public BreastModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            super(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror);
        }

        @Override
        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PositionTextureVertex vertex,
                                 PositionTextureVertex vertex1, PositionTextureVertex vertex2, PositionTextureVertex vertex3, PositionTextureVertex vertex4, PositionTextureVertex vertex5,
                                 PositionTextureVertex vertex6, PositionTextureVertex vertex7) {
            this.quads[0] = new TexturedQuad(texU + 4 + dx, texV + 4, texU + 4 + dx + 4, texV + 4 + dy, tW, tH, mirror, EnumFacing.EAST,
                    vertex4, vertex, vertex1, vertex5);
            this.quads[1] = new TexturedQuad(texU, texV + 4, texU + 4, texV + 4 + dy, tW, tH, mirror, EnumFacing.WEST,
                    vertex7, vertex3, vertex6, vertex2);
            this.quads[2] = new TexturedQuad(texU + 4, texV, texU + 4 + dx, texV + 4, tW, tH, mirror, EnumFacing.DOWN,
                    vertex4, vertex3, vertex7, vertex);
            this.quads[3] = new TexturedQuad(texU + 4, texV + 4 + 4, texU + 4 + dx, texV + 1 + 4 + dy, tW, tH - 1, mirror, EnumFacing.UP,
                    vertex1, vertex2, vertex6, vertex5);
            this.quads[4] = new TexturedQuad(texU + 4, texV + 4, texU + 4 + dx, texV + 4 + dy, tW, tH, mirror, EnumFacing.NORTH,
                    vertex, vertex7, vertex2, vertex1);
        }
    }

    public static class SkinnedModelBox extends ModelBox {

        public SkinnedModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            super(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, 6);
        }

        @Override
        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PositionTextureVertex vertex,
                                 PositionTextureVertex vertex1, PositionTextureVertex vertex2, PositionTextureVertex vertex3, PositionTextureVertex vertex4, PositionTextureVertex vertex5,
                                 PositionTextureVertex vertex6, PositionTextureVertex vertex7) {
            this.quads[0] = new TexturedQuad(texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, tW, tH, mirror, EnumFacing.EAST,
                    vertex4, vertex, vertex1, vertex5);
            this.quads[1] = new TexturedQuad(texU, texV + dz, texU + dz, texV + dz + dy, tW, tH, mirror, EnumFacing.WEST,
                    vertex7, vertex3, vertex6, vertex2);
            this.quads[2] = new TexturedQuad(texU + dz, texV, texU + dz + dx, texV + dz, tW, tH, mirror, EnumFacing.DOWN,
                    vertex4, vertex3, vertex7, vertex);
            this.quads[3] = new TexturedQuad(texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, tW, tH, mirror, EnumFacing.UP,
                    vertex1, vertex2, vertex6, vertex5);
            this.quads[4] = new TexturedQuad(texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, tW, tH, mirror, EnumFacing.NORTH,
                    vertex, vertex7, vertex2, vertex1);
            this.quads[5] = new TexturedQuad(texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, tW, tH, mirror, EnumFacing.SOUTH,
                    vertex3, vertex4, vertex5, vertex6);
        }
    }

    public static class SkinnedModelPlane {
        public final WildfireModelRenderer.TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posZ2;

        public SkinnedModelPlane(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dz, float delta, boolean mirror) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float) dx;
            this.posZ2 = z + (float) dz;
            this.quads = new TexturedQuad[1];
            float f = x + (float) dx;
            float f2 = z + (float) dz;
            x = x - delta;
            y = y - delta;
            z = z - delta;
            f = f + delta;
            f2 = f2 + delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }

            PositionTextureVertex vertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex vertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex vertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex vertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);

            this.quads[0] = new TexturedQuad(texU, texV, texU + dz, texV + dz, tW, tH, mirror, EnumFacing.EAST, vertex4, vertex3, vertex7, vertex);

        }
    }

    public record PositionTextureVertex(float x, float y, float z, float texturePositionX, float texturePositionY) {

        public PositionTextureVertex withTexturePosition(float texU, float texV) {
            return new PositionTextureVertex(x, y, z, texU, texV);
        }
    }

    public static class TexturedQuad {
        public final WildfireModelRenderer.PositionTextureVertex[] vertexPositions;
        public Vec3i normal;

        public TexturedQuad(float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, EnumFacing directionIn, PositionTextureVertex... positionsIn) {
            if (positionsIn.length != 4) {
                throw new IllegalArgumentException("Wrong number of vertex's. Expected: 4, Received: " + positionsIn.length);
            }
            this.vertexPositions = positionsIn;
            float f = 0.0F / texWidth;
            float f1 = 0.0F / texHeight;
            positionsIn[0] = positionsIn[0].withTexturePosition(u2 / texWidth - f, v1 / texHeight + f1);
            positionsIn[1] = positionsIn[1].withTexturePosition(u1 / texWidth + f, v1 / texHeight + f1);
            positionsIn[2] = positionsIn[2].withTexturePosition(u1 / texWidth + f, v2 / texHeight - f1);
            positionsIn[3] = positionsIn[3].withTexturePosition(u2 / texWidth - f, v2 / texHeight - f1);
            if (mirrorIn) {
                int i = positionsIn.length;

                for (int j = 0; j < i / 2; ++j) {
                    WildfireModelRenderer.PositionTextureVertex vertex = positionsIn[j];
                    positionsIn[j] = positionsIn[i - 1 - j];
                    positionsIn[i - 1 - j] = vertex;
                }
            }

            this.normal = directionIn.getDirectionVec();
            if (mirrorIn) {
                normal = new Vec3i(normal.getX() * -1, normal.getY() * -1, normal.getZ() * -1);
            }

        }
    }
}
