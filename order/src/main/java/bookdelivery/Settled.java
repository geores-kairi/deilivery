
package bookdeliverygeores;

public class Settled extends AbstractEvent {

    private Long settkeid;
    private Long orderid;
    private Long itemid;
    private Integer qty;
    private Integer itemPrice;
    private String orderStatus;

    public Long getId() {
        return settkeid;
    }

    public void setId(Long settkeid) {
        this.settkeid = settkeid;
    }
    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
    public Long getItemid() {
        return itemid;
    }

    public void setItemid(Long itemid) {
        this.itemid = itemid;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public Integer getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}

