package exchangetask;

import java.util.*;

public class Exchange implements ExchangeInterface, QueryInterface {
    private SortedSet<Order> sellOrders = new TreeSet<>(Comparator.comparing(Order::getPrice, Comparator.<Integer>naturalOrder()).thenComparing(Order::getRef));

    private SortedSet<Order> buyOrders = new TreeSet<>(Comparator.comparing(Order::getPrice, Comparator.<Integer>naturalOrder().reversed()).thenComparing(Order::getRef));

    private Map<Order, Order> orders = new HashMap<>();

    private final Object monitor = new Object();

    private int ref;

    @Override
    public void send(long orderId, boolean isBuy, int price, int size) throws RequestRejectedException {
        if (price < 0 || size < 0) {
            throw new RequestRejectedException();
        }
        synchronized (monitor) {
            ++ref;
            Order order = new Order(orderId, price, size, ++ref);
            Order otherOrder = orders.putIfAbsent(order, order);
            if (otherOrder != null) {
                throw new RequestRejectedException();
            }

            if (isBuy) {
                buyOrders.add(order);
            } else {
                sellOrders.add(order);
            }
            while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
                Order buy = buyOrders.first();
                Order sell = sellOrders.first();
                if (buy.getPrice() < sell.getPrice()) {
                    break;
                }
                int amount = Math.min(buy.getSize(), sell.getSize());
                buy.setSize(buy.getSize() - amount);
                sell.setSize(sell.getSize() - amount);
                if (buy.getSize() == 0) {
                    buyOrders.remove(buy);
                    orders.remove(buy);
                }
                if (sell.getSize() == 0) {
                    sellOrders.remove(sell);
                    orders.remove(sell);
                }
            }
        }
    }

    @Override
    public void cancel(long orderId) throws RequestRejectedException {
        synchronized (monitor) {
            Order order = orders.remove(new Order(orderId, 0, 0, 0));
            if (order == null) {
                throw new RequestRejectedException();
            }
            sellOrders.remove(order);
            buyOrders.remove(order);
        }
    }

    @Override
    public int getTotalSizeAtPrice(int price) throws RequestRejectedException {
        if (price < 0) {
            throw new RequestRejectedException();
        }
        Order l = new Order(price, Integer.MIN_VALUE);
        Order r = new Order(price, Integer.MAX_VALUE);
        synchronized (monitor) {
            return sellOrders.subSet(l, r).stream().mapToInt(Order::getSize).sum() + buyOrders.subSet(l, r).stream().mapToInt(Order::getSize).sum();
        }
    }

    @Override
    public int getHighestBuyPrice() throws RequestRejectedException {
        synchronized (monitor) {
            if (buyOrders.isEmpty()) {
                throw new RequestRejectedException();
            }
            return buyOrders.first().getPrice();
        }
    }

    @Override
    public int getLowestSellPrice() throws RequestRejectedException {
        synchronized (monitor) {
            if (sellOrders.isEmpty()) {
                throw new RequestRejectedException();
            }
            return sellOrders.first().getPrice();
        }
    }
}
