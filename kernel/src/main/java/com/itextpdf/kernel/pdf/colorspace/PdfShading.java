package com.itextpdf.kernel.pdf.colorspace;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfObjectWrapper;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.function.PdfFunction;

public abstract class PdfShading<T extends PdfDictionary> extends PdfObjectWrapper<T> {

    private static class ShadingType {
        public static final int FunctionBased = 1;
        public static final int Axial = 2;
        public static final int Radial = 3;
        public static final int FreeFormGouraudShadedTriangleMesh = 4;
        public static final int LatticeFormGouraudShadedTriangleMesh = 5;
        public static final int CoonsPatchMesh = 6;
        public static final int TensorProductPatchMesh = 7;
    }

    public PdfShading(T pdfObject) {
        super(pdfObject);
    }

    public PdfShading(T pdfObject, int type, PdfObject colorSpace) {
        super(pdfObject);

        getPdfObject().put(PdfName.ShadingType, new PdfNumber(type));
        getPdfObject().put(PdfName.ColorSpace, colorSpace);
    }

    public int getShadingType() {
        return getPdfObject().getAsInt(PdfName.ShadingType);
    }

    public PdfObject getColorSpace() {
        return getPdfObject().get(PdfName.ColorSpace);
    }

    public PdfObject getFunction() {
        return getPdfObject().get(PdfName.Function);
    }

    public void setFunction(PdfFunction function) {
        getPdfObject().put(PdfName.Function, function.getPdfObject());
        setModified();
    }

    public void setFunction(PdfFunction[] functions) {
        PdfArray arr = new PdfArray();
        for (PdfFunction func : functions) {
            arr.add(func.getPdfObject());
        }
        getPdfObject().put(PdfName.Function, arr);
        setModified();
    }

    @Override
    protected boolean isWrappedObjectMustBeIndirect() {
        return true;
    }

    static public class FunctionBased extends PdfShading<PdfDictionary> {

        public FunctionBased(PdfDictionary pdfObject) {
            super(pdfObject);
        }

        public FunctionBased(PdfColorSpace colorSpace, PdfFunction function) {
            this(colorSpace.getPdfObject(), function);
        }

        public FunctionBased(PdfObject colorSpace, PdfFunction function) {
            super(new PdfDictionary(), ShadingType.FunctionBased, colorSpace);

            setFunction(function);
        }

        public PdfArray getDomain() {
            return getPdfObject().getAsArray(PdfName.Domain);
        }

        public void setDomain(float xmin, float xmax, float ymin, float ymax) {
            getPdfObject().put(PdfName.Domain, new PdfArray(new float[] {xmin, xmax, ymin, ymax}));
            setModified();
        }

        public void setDomain(PdfArray domain) {
            getPdfObject().put(PdfName.Domain, domain);
            setModified();
        }

        public float[] getMatrix() {
            PdfArray matrix = getPdfObject().getAsArray(PdfName.Matrix);
            if (matrix == null)
                return new float[] {1, 0, 0, 1, 0, 0};
            float[] result = new float[6];
            for (int i = 0; i < 6; i++)
                result[i] = matrix.getAsFloat(i);
            return result;
        }

        public void setMatrix(float[] matrix) {
            setMatrix(new PdfArray(matrix));
        }

        public void setMatrix(PdfArray matrix) {
            getPdfObject().put(PdfName.Matrix, matrix);
            setModified();
        }
    }

    static public class Axial extends PdfShading<PdfDictionary> {

        public Axial(PdfDictionary pdfDictionary) {
            super(pdfDictionary);
        }

        public Axial(PdfColorSpace cs, float x0, float y0, float[] color0, float x1, float y1, float[] color1) {
            super(new PdfDictionary(), ShadingType.Axial, cs.getPdfObject());

            if (cs instanceof PdfSpecialCs.Pattern)
                throw new IllegalArgumentException("colorSpace");

            setCoords(x0, y0, x1, y1);
            PdfFunction func = new PdfFunction.Type2(new PdfArray(new float[] {0, 1}), null,
                    new PdfArray(color0), new PdfArray(color1), new PdfNumber(1));
            setFunction(func);
        }

        public Axial(PdfColorSpace cs, float x0, float y0, float[] color0, float x1, float y1, float[] color1, boolean[] extend) {
            this(cs, x0, y0, color0, x1, y1, color1);

            if (extend != null)
                setExtend(extend[0], extend[1]);
        }

        public Axial(PdfColorSpace cs, PdfArray coords, PdfFunction function) {
            super(new PdfDictionary(), ShadingType.Axial, cs.getPdfObject());
            setCoords(coords);
            setFunction(function);
        }

        public PdfArray getCoords() {
            return getPdfObject().getAsArray(PdfName.Coords);
        }

        public void setCoords(float x0, float y0, float x1, float y1) {
            setCoords(new PdfArray(new float[] {x0, y0, x1, y1}));
        }

        public void setCoords(PdfArray coords) {
            getPdfObject().put(PdfName.Coords, coords);
            setModified();
        }

        public float[] getDomain() {
            PdfArray domain = getPdfObject().getAsArray(PdfName.Domain);
            if (domain == null)
                return new float[] {0, 1};
            return new float[] {domain.getAsFloat(0), domain.getAsFloat(1)};
        }

        public void setDomain(float t0, float t1) {
            getPdfObject().put(PdfName.Domain, new PdfArray(new float[] {t0, t1}));
            setModified();
        }

        public boolean[] getExtend() {
            PdfArray extend = getPdfObject().getAsArray(PdfName.Extend);
            if (extend == null)
                return new boolean[] {true, true};
            return new boolean[] {extend.getAsBool(0), extend.getAsBool(1)};
        }

        public void setExtend(boolean extendStart, boolean extendEnd) {
            getPdfObject().put(PdfName.Extend, new PdfArray(new boolean[] {extendStart, extendEnd}));
            setModified();
        }

    }

    static public class Radial extends PdfShading<PdfDictionary> {
        public Radial(PdfDictionary pdfDictionary) {
            super(pdfDictionary);
        }

        public Radial(PdfColorSpace cs, float x0, float y0, float r0, float[] color0, float x1, float y1, float r1, float[] color1) {
            super(new PdfDictionary(), ShadingType.Radial, cs.getPdfObject());

            setCoords(x0, y0, r0, x1, y1, r1);
            PdfFunction func = new PdfFunction.Type2(new PdfArray(new float[] {0, 1}), null,
                    new PdfArray(color0), new PdfArray(color1), new PdfNumber(1));
            setFunction(func);
        }

        public Radial(PdfColorSpace cs, float x0, float y0, float r0, float[] color0, float x1, float y1, float r1, float[] color1, boolean[] extend) {
            this(cs, x0, y0, r0, color0, x1, y1, r1, color1);

            if (extend != null)
                setExtend(extend[0], extend[1]);
        }

        public Radial(PdfColorSpace cs, PdfArray coords, PdfFunction function) {
            super(new PdfDictionary(), ShadingType.Radial, cs.getPdfObject());
            setCoords(coords);
            setFunction(function);
        }

        public PdfArray getCoords() {
            return getPdfObject().getAsArray(PdfName.Coords);
        }

        public void setCoords(float x0, float y0, float r0, float x1, float y1, float r1) {
            setCoords(new PdfArray(new float[] {x0, y0, r0, x1, y1, r1}));
        }

        public void setCoords(PdfArray coords) {
            getPdfObject().put(PdfName.Coords, coords);
            setModified();
        }

        public float[] getDomain() {
            PdfArray domain = getPdfObject().getAsArray(PdfName.Domain);
            if (domain == null)
                return new float[] {0, 1};
            return new float[] {domain.getAsFloat(0), domain.getAsFloat(1)};
        }

        public void setDomain(float t0, float t1) {
            getPdfObject().put(PdfName.Domain, new PdfArray(new float[] {t0, t1}));
            setModified();
        }

        public boolean[] getExtend() {
            PdfArray extend = getPdfObject().getAsArray(PdfName.Extend);
            if (extend == null)
                return new boolean[] {true, true};
            return new boolean[] {extend.getAsBool(0), extend.getAsBool(1)};
        }

        public void setExtend(boolean extendStart, boolean extendEnd) {
            getPdfObject().put(PdfName.Extend, new PdfArray(new boolean[] {extendStart, extendEnd}));
            setModified();
        }
    }

    static public class FreeFormGouraudShadedTriangleMesh extends PdfShading<PdfStream> {
        public FreeFormGouraudShadedTriangleMesh(PdfStream pdfStream) {
            super(pdfStream);
        }

        public FreeFormGouraudShadedTriangleMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int bitsPerFlag, float[] decode) {
            this(cs, bitsPerCoordinate, bitsPerComponent, bitsPerFlag, new PdfArray(decode));
        }

        public FreeFormGouraudShadedTriangleMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int bitsPerFlag, PdfArray decode) {
            super(new PdfStream(), ShadingType.FreeFormGouraudShadedTriangleMesh, cs.getPdfObject());

            setBitsPerCoordinate(bitsPerCoordinate);
            setBitsPerComponent(bitsPerComponent);
            setBitsPerFlag(bitsPerFlag);
            setDecode(decode);
        }

        public int getBitsPerCoordinate() {
            return getPdfObject().getAsInt(PdfName.BitsPerCoordinate);
        }

        public void setBitsPerCoordinate(int bitsPerCoordinate) {
            getPdfObject().put(PdfName.BitsPerCoordinate, new PdfNumber(bitsPerCoordinate));
            setModified();
        }

        public int getBitsPerComponent() {
            return getPdfObject().getAsInt(PdfName.BitsPerComponent);
        }

        public void setBitsPerComponent(int bitsPerComponent) {
            getPdfObject().put(PdfName.BitsPerComponent, new PdfNumber(bitsPerComponent));
            setModified();
        }

        public int getBitsPerFlag() {
            return getPdfObject().getAsInt(PdfName.BitsPerFlag);
        }

        public void setBitsPerFlag(int bitsPerFlag) {
            getPdfObject().put(PdfName.BitsPerFlag, new PdfNumber(bitsPerFlag));
            setModified();
        }

        public PdfArray getDecode() {
            return getPdfObject().getAsArray(PdfName.Decode);
        }

        public void setDecode(float[] decode) {
            getPdfObject().put(PdfName.Decode, new PdfArray(decode));
        }

        public void setDecode(PdfArray decode) {
            getPdfObject().put(PdfName.Decode, decode);
        }
    }

    static public class LatticeFormGouraudShadedTriangleMesh extends PdfShading<PdfStream> {
        public LatticeFormGouraudShadedTriangleMesh(PdfStream pdfStream) {
            super(pdfStream);
        }

        public LatticeFormGouraudShadedTriangleMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int verticesPerRow, float[] decode) {
            this(cs, bitsPerCoordinate, bitsPerComponent, verticesPerRow, new PdfArray(decode));
        }

        public LatticeFormGouraudShadedTriangleMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int verticesPerRow, PdfArray decode) {
            super(new PdfStream(), ShadingType.LatticeFormGouraudShadedTriangleMesh, cs.getPdfObject());

            setBitsPerCoordinate(bitsPerCoordinate);
            setBitsPerComponent(bitsPerComponent);
            setVerticesPerRow(verticesPerRow);
            setDecode(decode);
        }

        public int getBitsPerCoordinate() {
            return getPdfObject().getAsInt(PdfName.BitsPerCoordinate);
        }

        public void setBitsPerCoordinate(int bitsPerCoordinate) {
            getPdfObject().put(PdfName.BitsPerCoordinate, new PdfNumber(bitsPerCoordinate));
            setModified();
        }

        public int getBitsPerComponent() {
            return getPdfObject().getAsInt(PdfName.BitsPerComponent);
        }

        public void setBitsPerComponent(int bitsPerComponent) {
            getPdfObject().put(PdfName.BitsPerComponent, new PdfNumber(bitsPerComponent));
            setModified();
        }

        public int getVerticesPerRow() {
            return getPdfObject().getAsInt(PdfName.VerticesPerRow);
        }

        public void setVerticesPerRow(int verticesPerRow) {
            getPdfObject().put(PdfName.VerticesPerRow, new PdfNumber(verticesPerRow));
            setModified();
        }

        public PdfArray getDecode() {
            return getPdfObject().getAsArray(PdfName.Decode);
        }

        public void setDecode(float[] decode) {
            getPdfObject().put(PdfName.Decode, new PdfArray(decode));
        }

        public void setDecode(PdfArray decode) {
            getPdfObject().put(PdfName.Decode, decode);
        }
    }

    static public class CoonsPatchMesh extends PdfShading<PdfStream> {
        public CoonsPatchMesh(PdfStream pdfStream) {
            super(pdfStream);
        }

        public CoonsPatchMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int bitsPerFlag, float[] decode) {
            this(cs, bitsPerCoordinate, bitsPerComponent, bitsPerFlag, new PdfArray(decode));
        }

        public CoonsPatchMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int bitsPerFlag, PdfArray decode) {
            super(new PdfStream(), ShadingType.CoonsPatchMesh, cs.getPdfObject());
            setBitsPerCoordinate(bitsPerCoordinate);
            setBitsPerComponent(bitsPerComponent);
            setBitsPerFlag(bitsPerFlag);
            setDecode(decode);
        }

        public int getBitsPerCoordinate() {
            return getPdfObject().getAsInt(PdfName.BitsPerCoordinate);
        }

        public void setBitsPerCoordinate(int bitsPerCoordinate) {
            getPdfObject().put(PdfName.BitsPerCoordinate, new PdfNumber(bitsPerCoordinate));
            setModified();
        }

        public int getBitsPerComponent() {
            return getPdfObject().getAsInt(PdfName.BitsPerComponent);
        }

        public void setBitsPerComponent(int bitsPerComponent) {
            getPdfObject().put(PdfName.BitsPerComponent, new PdfNumber(bitsPerComponent));
            setModified();
        }

        public int getBitsPerFlag() {
            return getPdfObject().getAsInt(PdfName.BitsPerFlag);
        }

        public void setBitsPerFlag(int bitsPerFlag) {
            getPdfObject().put(PdfName.BitsPerFlag, new PdfNumber(bitsPerFlag));
            setModified();
        }

        public PdfArray getDecode() {
            return getPdfObject().getAsArray(PdfName.Decode);
        }

        public void setDecode(float[] decode) {
            getPdfObject().put(PdfName.Decode, new PdfArray(decode));
        }

        public void setDecode(PdfArray decode) {
            getPdfObject().put(PdfName.Decode, decode);
        }
    }

    static public class TensorProductPatchMesh extends PdfShading<PdfStream> {
        public TensorProductPatchMesh(PdfStream pdfStream) {
            super(pdfStream);
        }

        public TensorProductPatchMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int bitsPerFlag, float[] decode) {
            this(cs, bitsPerCoordinate, bitsPerComponent, bitsPerFlag, new PdfArray(decode));
        }

        public TensorProductPatchMesh(PdfColorSpace cs, int bitsPerCoordinate, int bitsPerComponent, int bitsPerFlag, PdfArray decode) {
            super(new PdfStream(), ShadingType.TensorProductPatchMesh, cs.getPdfObject());

            setBitsPerCoordinate(bitsPerCoordinate);
            setBitsPerComponent(bitsPerComponent);
            setBitsPerFlag(bitsPerFlag);
            setDecode(decode);
        }

        public int getBitsPerCoordinate() {
            return getPdfObject().getAsInt(PdfName.BitsPerCoordinate);
        }

        public void setBitsPerCoordinate(int bitsPerCoordinate) {
            getPdfObject().put(PdfName.BitsPerCoordinate, new PdfNumber(bitsPerCoordinate));
            setModified();
        }

        public int getBitsPerComponent() {
            return getPdfObject().getAsInt(PdfName.BitsPerComponent);
        }

        public void setBitsPerComponent(int bitsPerComponent) {
            getPdfObject().put(PdfName.BitsPerComponent, new PdfNumber(bitsPerComponent));
            setModified();
        }

        public int getBitsPerFlag() {
            return getPdfObject().getAsInt(PdfName.BitsPerFlag);
        }

        public void setBitsPerFlag(int bitsPerFlag) {
            getPdfObject().put(PdfName.BitsPerFlag, new PdfNumber(bitsPerFlag));
            setModified();
        }

        public PdfArray getDecode() {
            return getPdfObject().getAsArray(PdfName.Decode);
        }

        public void setDecode(float[] decode) {
            getPdfObject().put(PdfName.Decode, new PdfArray(decode));
        }

        public void setDecode(PdfArray decode) {
            getPdfObject().put(PdfName.Decode, decode);
        }
    }
}
