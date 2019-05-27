package exchangetask;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ExchangeTest {
    @Test
    public void testSimple() throws RequestRejectedException {
        Exchange exchange = new Exchange();

        exchange.send(1L, true, 5, 10);

        Assert.assertEquals(exchange.getHighestBuyPrice(), 5);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 10);

        exchange.send(2L, false, 5, 10);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 0);

        try {
            Assert.assertEquals(exchange.getHighestBuyPrice(), 5);
            Assert.fail();
        } catch (RequestRejectedException e) {
            //
        }
        try {
            Assert.assertEquals(exchange.getLowestSellPrice(), 5);
            Assert.fail();
        } catch (RequestRejectedException e) {
            //
        }
    }

    @Test
    public void testPartial() throws RequestRejectedException {
        Exchange exchange = new Exchange();

        exchange.send(1L, true, 5, 10);
        exchange.send(2L, false, 5, 1);
        exchange.send(3L, false, 5, 8);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 1);
        Assert.assertEquals(exchange.getHighestBuyPrice(), 5);
        exchange.send(4L, false, 5, 3);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 2);
        exchange.send(1L, true, 5, 2);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 0);
    }

    @Test
    public void testOrder() throws RequestRejectedException {
        Exchange exchange = new Exchange();

        exchange.send(3L, true, 5, 10);
        exchange.send(2L, true, 5, 10);
        exchange.send(1L, true, 5, 10);

        exchange.send(4L, false, 5, 20);

        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 10);
        exchange.cancel(1L);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 0);

    }

    @Test
    public void testPriceOrder() throws RequestRejectedException {
        Exchange exchange = new Exchange();

        exchange.send(1L, true, 5, 10);
        exchange.send(2L, true, 6, 10);

        exchange.send(3L, false, 5, 10);

        try {
            exchange.cancel(2L);
            Assert.fail();
        } catch (RequestRejectedException e) {
            //
        }
        exchange.cancel(1L);
    }

    @Test
    public void testHold() throws RequestRejectedException {
        Exchange exchange = new Exchange();

        exchange.send(1L, true, 5, 1);
        exchange.send(2L, true, 6, 2);
        exchange.send(3L, false, 7, 3);
        exchange.send(4L, false, 8, 4);

        Assert.assertEquals(exchange.getTotalSizeAtPrice(5), 1);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(6), 2);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(7), 3);
        Assert.assertEquals(exchange.getTotalSizeAtPrice(8), 4);
        Assert.assertEquals(exchange.getLowestSellPrice(), 7);
        Assert.assertEquals(exchange.getHighestBuyPrice(), 6);

        exchange.cancel(2L);
        Assert.assertEquals(exchange.getHighestBuyPrice(), 5);
    }

}