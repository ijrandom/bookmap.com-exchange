package exchangetask;

import java.util.Comparator;
import java.util.Objects;

public class Order {
    public static final Comparator<Order> ORDER_COMPARATOR = Comparator.comparing(Order::getPrice).thenComparing(Order::getId);

    private long id;

    private int price;

    private int size;

    private int ref;

    public Order(int price, int ref) {
        this.price = price;
        this.ref = ref;
    }

    public Order(long id, int price, int size, int ref) {
        this.id = id;
        this.price = price;
        this.size = size;
        this.ref = ref;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getRef() {
        return ref;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
