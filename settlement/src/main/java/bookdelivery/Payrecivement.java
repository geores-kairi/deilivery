package bookdelivery;

public class Payrecivement extends AbstractEvent {

    private Long paymentid;
    private Long settlementid;
    private Long orderId;
    private Long itemid;
    private Integer itemPrice;
    private Integer qty;
    private String orderStatus;

    public Payrecivement(){
        super();
    }

    public Long getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(Long paymentid) {
        this.paymentid = paymentid;
    }
    public Long getSettlementid() {
        return settlementid;
    }

    public void setSettlementid(Long settlementid) {
        this.settlementid = settlementid;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getItemid() {
        return itemid;
    }

    public void setItemid(Long itemid) {
        this.itemid = itemid;
    }
    public Integer getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
