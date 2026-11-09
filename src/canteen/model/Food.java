package canteen.model;

/** One row of FOOD_ITEMS (joined with its category name). */
public class Food {
    public int id;
    public String name;
    public int categoryId;
    public String categoryName;
    public double price;
    public boolean available;
    public String imagePath;
    public String description;
}
