package mizdooni.model;

public class Rating {
    public double food;
    public double service;
    public double ambiance;
    public double overall;

    public int getStarCount() {
        return (int) Math.min(Math.round(overall), 5);
    }
    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        Rating r = (Rating) obj;
        if(food == r.food && ambiance == r.ambiance && overall == r.overall && service == r.service){
            return true;
        }
        return false;
    }
}
