package canteen.model;

/** A food item + quantity inside the customer's cart. */
public class CartItem {
    public final Food food;
    public int qty;
    public CartItem(Food food, int qty) { this.food = food; this.qty = qty; }
    public double subtotal() { return food.price * qty; }
}
