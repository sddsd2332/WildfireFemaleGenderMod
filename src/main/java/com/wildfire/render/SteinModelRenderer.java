//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wildfire.render;


import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
/**
 * 1.12 版自定义模型盒工具：提供胸部、外层、贴皮盒子的顶点和 UV 展开。
 */
public class SteinModelRenderer {
    public SteinModelRenderer() {
    }

    @SideOnly(Side.CLIENT)
    public static class TexturedQuad {
        public final PositionTextureVertex[] vertexPositions;
        public Vec3d normal;

        public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, EnumFacing directionIn) {
            this.vertexPositions = positionsIn;
            float f = 0.0F / texWidth;
            float f1 = 0.0F / texHeight;
            positionsIn[0] = positionsIn[0].setTexturePosition(u2 / texWidth - f, v1 / texHeight + f1);
            positionsIn[1] = positionsIn[1].setTexturePosition(u1 / texWidth + f, v1 / texHeight + f1);
            positionsIn[2] = positionsIn[2].setTexturePosition(u1 / texWidth + f, v2 / texHeight - f1);
            positionsIn[3] = positionsIn[3].setTexturePosition(u2 / texWidth - f, v2 / texHeight - f1);
            if (mirrorIn) {
                int i = positionsIn.length;

                for (int j = 0; j < i / 2; ++j) {
                    PositionTextureVertex modelrenderer$positiontexturevertex = positionsIn[j];
                    positionsIn[j] = positionsIn[i - 1 - j];
                    positionsIn[i - 1 - j] = modelrenderer$positiontexturevertex;
                }
            }

            Vec3d normal = new Vec3d(directionIn.getDirectionVec());
            this.normal = new Vec3d(normal.x, normal.y, normal.z);
            if (mirrorIn) {
                this.normal = new Vec3d(normal.x * -1, normal.y * 1, normal.z * 1);
            }

        }
    }

    @SideOnly(Side.CLIENT)
    public static class PositionTextureVertex {
        public final Vec3d vector3D;
        public final float texturePositionX;
        public final float texturePositionY;

        public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
            this(new Vec3d(x, y, z), texU, texV);
        }

        public PositionTextureVertex setTexturePosition(float texU, float texV) {
            return new PositionTextureVertex(this.vector3D, texU, texV);
        }

        public PositionTextureVertex(Vec3d posIn, float texU, float texV) {
            this.vector3D = posIn;
            this.texturePositionX = texU;
            this.texturePositionY = texV;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class SkinnedModelPlane {
        public final TexturedQuad[] quads;
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
            x -= delta;
            y -= delta;
            z -= delta;
            f += delta;
            f2 += delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
            this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, (float) texU, (float) texV, (float) (texU + dz), (float) (texV + dz), (float) tW, (float) tH, mirror, EnumFacing.EAST);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class SkinnedModelBox {
        public final TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public SkinnedModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float) dx;
            this.posY2 = y + (float) dy;
            this.posZ2 = z + (float) dz;
            this.quads = new TexturedQuad[6];
            float f = x + (float) dx;
            float f1 = y + (float) dy;
            float f2 = z + (float) dz;
            x -= delta;
            y -= delta;
            z -= delta;
            f += delta;
            f1 += delta;
            f2 += delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
            this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, (float) (texU + dz + dx), (float) (texV + dz), (float) (texU + dz + dx + dz), (float) (texV + dz + dy), (float) tW, (float) tH, mirror, EnumFacing.EAST);
            this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, (float) texU, (float) (texV + dz), (float) (texU + dz), (float) (texV + dz + dy), (float) tW, (float) tH, mirror, EnumFacing.WEST);
            this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, (float) (texU + dz), (float) texV, (float) (texU + dz + dx), (float) (texV + dz), (float) tW, (float) tH, mirror, EnumFacing.DOWN);
            this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, (float) (texU + dz + dx), (float) (texV + dz), (float) (texU + dz + dx + dx), (float) texV, (float) tW, (float) tH, mirror, EnumFacing.UP);
            this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, (float) (texU + dz), (float) (texV + dz), (float) (texU + dz + dx), (float) (texV + dz + dy), (float) tW, (float) tH, mirror, EnumFacing.NORTH);
            this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, (float) (texU + dz + dx + dz), (float) (texV + dz), (float) (texU + dz + dx + dz + dx), (float) (texV + dz + dy), (float) tW, (float) tH, mirror, EnumFacing.SOUTH);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class BreastModelBox {
        public final TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public BreastModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float) dx;
            this.posY2 = y + (float) dy;
            this.posZ2 = z + (float) dz;
            this.quads = new TexturedQuad[5];
            float f = x + (float) dx;
            float f1 = y + (float) dy;
            float f2 = z + (float) dz;
            x -= delta;
            y -= delta;
            z -= delta;
            f += delta;
            f1 += delta;
            f2 += delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
            this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, (float)(texU + 4 + dx), (float)(texV + 4), (float)(texU + 4 + dx + 4), (float)(texV + 4 + dy), (float)tW, (float)tH, mirror, EnumFacing.EAST);
            this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, (float)texU, (float)(texV + 4), (float)(texU + 4), (float)(texV + 4 + dy), (float)tW, (float)tH, mirror, EnumFacing.WEST);
            this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, (float)(texU + 4), (float)texV, (float)(texU + 4 + dx), (float)(texV + 4), (float)tW, (float)tH, mirror, EnumFacing.DOWN);
            this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, (float)(texU + 4), (float)(texV + 8), (float)(texU + 4 + dx), (float)(texV + 5 + dy), (float)tW, (float)(tH - 1), mirror, EnumFacing.UP);
            this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, (float)(texU + 4), (float)(texV + 4), (float)(texU + 4 + dx), (float)(texV + 4 + dy), (float)tW, (float)tH, mirror, EnumFacing.NORTH);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class OverlayModelBox {
        public final TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public OverlayModelBox(boolean isLeft, int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float)dx;
            this.posY2 = y + (float)dy;
            this.posZ2 = z + (float)dz;
            this.quads = new TexturedQuad[4];
            float f = x + (float)dx;
            float f1 = y + (float)dy;
            float f2 = z + (float)dz;
            x -= delta;
            y -= delta;
            z -= delta;
            f += delta;
            f1 += delta;
            f2 += delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
            if (!isLeft) {
                this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, (float)(texU + dz + dx), (float)(texV + dz), (float)(texU + dz + dx + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, EnumFacing.EAST);
            } else {
                this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, (float)texU, (float)(texV + dz), (float)(texU + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, EnumFacing.WEST);
            }

            this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, (float)(texU + dz), (float)texV, (float)(texU + dz + dx), (float)(texV + dz), (float)tW, (float)tH, mirror, EnumFacing.DOWN);
            this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, (float)(texU + dz), (float)(texV + dz + 4), (float)(texU + dz + dx), (float)(texV + 1 + dz + dy), (float)tW, (float)(tH - 1), mirror, EnumFacing.UP);
            this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, (float)(texU + dz), (float)(texV + dz), (float)(texU + dz + dx), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, EnumFacing.NORTH);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class ModelBox {
        public final TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float)dx;
            this.posY2 = y + (float)dy;
            this.posZ2 = z + (float)dz;
            this.quads = new TexturedQuad[5];
            float f = x + (float)dx;
            float f1 = y + (float)dy;
            float f2 = z + (float)dz;
            x -= delta;
            y -= delta;
            z -= delta;
            f += delta;
            f1 += delta;
            f2 += delta;
            if (mirror) {
                float f3 = f;
                f = x;
                x = f3;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
            this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, (float)(texU + dz + dx), (float)(texV + dz), (float)(texU + dz + dx + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, EnumFacing.EAST);
            this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, (float)texU, (float)(texV + dz), (float)(texU + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, EnumFacing.WEST);
            this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, (float)(texU + dz), (float)texV, (float)(texU + dz + dx), (float)(texV + dz), (float)tW, (float)tH, mirror, EnumFacing.DOWN);
            this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, (float)(texU + dz), (float)(texV + dz + 4), (float)(texU + dz + dx), (float)(texV + 1 + dz + dy), (float)tW, (float)(tH - 1), mirror, EnumFacing.UP);
            this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, (float)(texU + dz), (float)(texV + dz), (float)(texU + dz + dx), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, EnumFacing.NORTH);
        }
    }
}
