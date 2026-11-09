package canteen.ui;

/** Owner: all orders with the full status workflow. */
public class OrdersPage extends BasePage {
    private final OrderBoard board = new OrderBoard(false);
    public OrdersPage() { add(board, java.awt.BorderLayout.CENTER); }
    @Override public void refresh() { board.refresh(); }
}
