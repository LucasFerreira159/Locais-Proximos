package pagar.me.locaisproximos.model;

public class Viewport {

    private SouthwestK southwest;

    private NortheastK northeast;

    public SouthwestK getSouthwest ()
    {
        return southwest;
    }

    public void setSouthwest (SouthwestK southwest)
    {
        this.southwest = southwest;
    }

    public NortheastK getNortheast ()
    {
        return northeast;
    }

    public void setNortheast (NortheastK northeast)
    {
        this.northeast = northeast;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [southwest = "+southwest+", northeast = "+northeast+"]";
    }

}
